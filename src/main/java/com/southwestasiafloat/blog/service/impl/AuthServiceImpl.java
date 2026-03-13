package com.southwestasiafloat.blog.service.impl;

import com.southwestasiafloat.blog.dto.UserLoginDto;
import com.southwestasiafloat.blog.dto.UserRegisterDto;
import com.southwestasiafloat.blog.entity.RefreshToken;
import com.southwestasiafloat.blog.entity.User;
import com.southwestasiafloat.blog.exception.AuthenticationException;
import com.southwestasiafloat.blog.repository.AuthRepository;
import com.southwestasiafloat.blog.service.AuthService;
import com.southwestasiafloat.blog.utils.JwtTokenProvider;
import com.southwestasiafloat.blog.utils.UserValidator;
import com.southwestasiafloat.blog.vo.AuthVo;
import com.southwestasiafloat.blog.vo.UserVo;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthServiceImpl(AuthRepository authRepository,
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider jwtTokenProvider) {
        this.authRepository = authRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public AuthVo login(UserLoginDto dto) throws Exception {
        if (dto == null) throw new IllegalArgumentException("登录参数不能为空");
        String username = UserValidator.normalizeUsername(dto.getUsername());
        String password = dto.getPassword();
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            throw new IllegalArgumentException("username and password are required");
        }

        User user = authRepository.findByUsername(username)
                .orElseThrow(() -> new AuthenticationException("用户不存在"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new AuthenticationException("密码错误");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getRole());
        String rawRefreshToken = generateRawRefreshToken();
        insertRefreshToken(user.getId(), rawRefreshToken, dto.getIp(), dto.getUserAgent());

        return AuthVo.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefreshToken)
                .expiresIn(jwtTokenProvider.getAccessTokenValidityInMillis())
                .userId(user.getId())
                .isAdmin(isAdminRole(user.getRole()))
                .build();
    }

    @Override
    @Transactional
    public UserVo register(UserRegisterDto dto) {
        if (dto == null) throw new IllegalArgumentException("用户信息不能为空");

        User toCreate = User.builder()
                .username(dto.getUsername())
                .password(dto.getPassword())
                .nickname(dto.getNickname())
                .email(dto.getEmail())
                .build();

        UserValidator.validateUserForRegister(toCreate, false);
        String username = UserValidator.normalizeUsername(toCreate.getUsername());
        String email = UserValidator.normalizeEmail(toCreate.getEmail());

        if (authRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("用户名已存在");
        }
        if (authRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("邮箱已存在");
        }

        LocalDateTime now = LocalDateTime.now();
        toCreate.setUsername(username);
        toCreate.setEmail(email);
        toCreate.setPassword(passwordEncoder.encode(toCreate.getPassword()));
        toCreate.setRole("USER");
        toCreate.setCreateTime(now);
        toCreate.setUpdateTime(now);

        authRepository.insertUser(toCreate);
        return toUserVo(toCreate);
    }

    @Override
    @Transactional
    public AuthVo refresh(String refreshToken, String ip, String userAgent) throws Exception {
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            throw new IllegalArgumentException("refreshToken 不能为空");
        }

        String oldHash = sha256(refreshToken);
        RefreshToken existing = authRepository.findRefreshTokenByHash(oldHash)
                .orElseThrow(() -> new AuthenticationException("refresh token 无效"));

        LocalDateTime now = LocalDateTime.now();
        if (Boolean.TRUE.equals(existing.getRevoked())) {
            throw new AuthenticationException("refresh token 已被撤销");
        }
        if (existing.getExpiresAt() == null || existing.getExpiresAt().isBefore(now)) {
            throw new AuthenticationException("refresh token 已过期");
        }

        User user = authRepository.findById(existing.getUserId())
                .orElseThrow(() -> new AuthenticationException("用户不存在"));

        // refresh token rotation：旧 token 作废，并发放新 token
        String newRaw = generateRawRefreshToken();
        String newHash = sha256(newRaw);
        existing.setRevoked(true);
        existing.setRevokedAt(now);
        existing.setReplacedBy(newHash);
        authRepository.updateRefreshToken(existing);

        insertRefreshToken(user.getId(), newRaw, ip, userAgent);

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getRole());
        return AuthVo.builder()
                .accessToken(accessToken)
                .refreshToken(newRaw)
                .expiresIn(jwtTokenProvider.getAccessTokenValidityInMillis())
                .userId(user.getId())
                .isAdmin(isAdminRole(user.getRole()))
                .build();
    }

    @Override
    @Transactional
    public void logout(String refreshToken) throws Exception {
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            throw new IllegalArgumentException("refreshToken 不能为空");
        }

        String hash = sha256(refreshToken);
        RefreshToken existing = authRepository.findRefreshTokenByHash(hash).orElse(null);

        // 幂等：token 不存在或已撤销都算成功
        if (existing == null || Boolean.TRUE.equals(existing.getRevoked())) {
            return;
        }

        existing.setRevoked(true);
        existing.setRevokedAt(LocalDateTime.now());
        authRepository.updateRefreshToken(existing);
    }

    private void insertRefreshToken(Long userId, String rawRefreshToken, String ip, String userAgent) throws Exception {
        RefreshToken token = new RefreshToken();
        token.setUserId(userId);
        token.setTokenHash(sha256(rawRefreshToken));
        token.setIssuedAt(LocalDateTime.now());
        long refreshMs = jwtTokenProvider.getRefreshTokenValidityInMillis() == null
                ? 0L
                : jwtTokenProvider.getRefreshTokenValidityInMillis();
        token.setExpiresAt(LocalDateTime.now().plusSeconds(refreshMs / 1000));
        token.setRevoked(false);
        token.setIp(ip);
        token.setUserAgent(userAgent);
        authRepository.insertRefreshToken(token);
    }

    private UserVo toUserVo(User user) {
        return UserVo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .role(user.getRole())
                .createTime(user.getCreateTime())
                .updateTime(user.getUpdateTime())
                .build();
    }

    private boolean isAdminRole(String role) {
        if (role == null) {
            return false;
        }
        return "ADMIN".equalsIgnoreCase(role) || "ROLE_ADMIN".equalsIgnoreCase(role);
    }

    private String generateRawRefreshToken() {
        return UUID.randomUUID() + "-" + UUID.randomUUID();
    }

    private String sha256(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
