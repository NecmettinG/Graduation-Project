import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  async rewrites() {
    const coreApi = process.env.NEXT_PUBLIC_CORE_API_URL || "http://localhost:8080/smarty-commerce";
    const recApi = process.env.NEXT_PUBLIC_REC_API_URL || "http://localhost:8000";

    return [
      {
        source: "/api/core/:path*",
        destination: `${coreApi}/:path*`,
      },
      {
        source: "/api/rec/:path*",
        destination: `${recApi}/:path*`,
      },
    ];
  },
};

export default nextConfig;
