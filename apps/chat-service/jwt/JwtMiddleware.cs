using System.Security.Claims;
using Microsoft.IdentityModel.Tokens;

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
            await context.Response.WriteAsync("Unauthorized");
            return;
        }
        Console.WriteLine($"[JWT] Auth header received: '{token}'");

        try
        {
            var userId = _jwtUtil.ValidateAndGetUserId(token);
            var claims = new[] { new Claim("userId", userId) };
            context.User = new ClaimsPrincipal(new ClaimsIdentity(claims, "jwt"));
        }
        catch (SecurityTokenExpiredException)
        {
            context.Response.StatusCode = 401;
            await context.Response.WriteAsync("Token expired");
            return;
        }
        catch (SecurityTokenException ex)
        {
            Console.WriteLine($"INVALID: {ex.Message}");
            context.Response.StatusCode = 401;
            await context.Response.WriteAsync("Invalid token");
            return;
        }
        catch (Exception ex)
        {
            Console.WriteLine($"OTHER: {ex.GetType().Name}: {ex.Message}");
            context.Response.StatusCode = 401;
            await context.Response.WriteAsync("Unauthorized");
            return;
        }

        await _next(context);
    }
}