using System.Security.Claims;
using Microsoft.IdentityModel.Tokens;
using OpenTelemetry.Trace;

public class JwtMiddleware
{
    private readonly RequestDelegate _next;
    private readonly JwtUtil _jwtUtil;

    public JwtMiddleware(RequestDelegate next, JwtUtil jwtUtil)
    {
        _next = next;
        _jwtUtil = jwtUtil;
    }

    public async Task InvokeAsync(HttpContext context)
    {
        var authHeader = context.Request.Headers["Authorization"].FirstOrDefault();
        var token = authHeader?.StartsWith("Bearer ") == true
            ? authHeader[7..]
            : context.Request.Query["token"].FirstOrDefault();

        if (string.IsNullOrWhiteSpace(token))
        {
            context.Response.StatusCode = 401;
            await errorMessage(401, "Unauthorized", context);
            return;
        }

        try
        {
            var userId = _jwtUtil.ValidateAndGetUserId(token);
            var claims = new[] { new Claim("userId", userId) };
            context.User = new ClaimsPrincipal(new ClaimsIdentity(claims, "jwt"));
        }
        catch (SecurityTokenExpiredException)
        {
            await errorMessage(401, "Token expired", context);
            return;
        }
        catch (SecurityTokenException ex)
        {
            await errorMessage(401, "Invalid token", context);
            return;
        }
        catch (Exception ex)
        {
            await errorMessage(401, "Unauthorized", context);
            return;
        }

        await _next(context);
    }

    private async Task errorMessage(int statuscode, string errorMessage, HttpContext context)
    {
        AppLogger.Error($"JWT: INVALID: {statuscode}{errorMessage}");
        context.Response.StatusCode = statuscode;
        await context.Response.WriteAsync(errorMessage);
        return;
    }
}