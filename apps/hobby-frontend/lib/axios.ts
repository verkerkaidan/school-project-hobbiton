// lib/axios.ts
import axios from "axios";
import Cookies from "js-cookie";


const api = axios.create({
    baseURL: process.env.NEXT_PUBLIC_CHAT_SERVICE_URL,
});

api.interceptors.request.use((config) => {
    if (typeof window === "undefined") {
        return config;
    }

    const token = Cookies.get("token");
    if (token) {
        config.headers = config.headers ?? {};
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

export default api;