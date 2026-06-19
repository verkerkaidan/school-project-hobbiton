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

api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401 && typeof window !== "undefined") {
            Cookies.remove("token");
            Cookies.remove("userId");
            Cookies.remove("email");
            window.location.href = "/authentication?expired=true";
        }
        return Promise.reject(error);
    }
);

export default api;