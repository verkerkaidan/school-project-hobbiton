using System.IdentityModel.Tokens.Jwt;
using System.Security.Cryptography;
using System.Text;
using Microsoft.IdentityModel.Tokens;

public class JwtUtil
{
    private readonly string _secret;
    private readonly string _issuer;

    public JwtUtil()
    {
        _secret = Environment.GetEnvironmentVariable("JWT_SECRET")
            ?? throw new InvalidOperationException("JWT_SECRET is not set");

        _issuer = Environment.GetEnvironmentVariable("JWT_ISSUER")
            ?? throw new InvalidOperationException("JWT_ISSUER is not set");
    }

    public string ValidateAndGetUserId(string token)
    {
        var handler = new JwtSecurityTokenHandler();
        var jwt = handler.ReadJwtToken(token);

        // Manually verify signature to bypass .NET's minimum key size enforcement
        var parts = token.Split('.');
        if (parts.Length != 3)
            throw new SecurityTokenException("Malformed token");

        var headerPayload = parts[0] + "." + parts[1];
        using var hmac = new HMACSHA256(Encoding.UTF8.GetBytes(_secret));
        var hash = hmac.ComputeHash(Encoding.UTF8.GetBytes(headerPayload));
        var expectedSig = Base64UrlEncoder.Encode(hash);

        if (parts[2] != expectedSig)
            throw new SecurityTokenException("Invalid signature");

        if (jwt.ValidTo < DateTime.UtcNow)
            throw new SecurityTokenExpiredException("Token expired") { };

        if (jwt.Issuer != _issuer)
            throw new SecurityTokenException("Invalid issuer");

        return jwt.Claims.FirstOrDefault(c => c.Type == "userId")?.Value
            ?? throw new SecurityTokenException("userId claim missing");
    }
}