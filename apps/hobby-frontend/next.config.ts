const nextConfig = {
    turbopack: {
        root: __dirname,
    },
    images: {
        remotePatterns: [
            {
                protocol: 'https',
                hostname: 'images.unsplash.com',
            },
            // Voeg hier andere bronnen toe indien nodig
        ],
    },
};
export default nextConfig;
