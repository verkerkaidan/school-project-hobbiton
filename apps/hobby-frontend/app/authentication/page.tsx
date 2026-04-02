"use client";

import { LoginModal } from "@/components/modals/LoginModal";
import { RegisterModal } from "@/components/modals/RegisterModal";
import { useState } from "react";

interface LoginForm {
    email: string;
    password: string;
}

interface RegisterForm {
    email: string;
    password: string;
    confirmPassword: string;
}
export default function Authentication() {
    const [loginOpen, setLoginOpen] = useState(false);
    const [registerOpen, setRegisterOpen] = useState(false);

    return (
        <div className="flex min-h-screen">
            <main className="flex-1 ml-20 md:ml-64 px-6 pt-16 pb-20">
                <header className="mb-20 space-y-4">
                    <h1 className="text-5xl md:text-7xl font-serif italic text-white">
                        Welcome <span className="text-brand">User</span>
                    </h1>
                    <p className="text-accent text-lg max-w-sm">
                        Jouw persoonlijke stroom van literatuur, cinema en beweging.
                    </p>
                </header>

                <div className="flex gap-4">
                    <button
                        onClick={() => setLoginOpen(true)}
                        className="px-6 py-2.5 rounded-lg border border-white/20 text-white text-sm font-medium hover:bg-white/10 transition-colors duration-200"
                    >
                        Login
                    </button>
                    <button
                        onClick={() => setRegisterOpen(true)}
                        className="px-6 py-2.5 rounded-lg bg-brand text-white text-sm font-medium hover:opacity-90 transition-opacity duration-200"
                    >
                        Register
                    </button>

                    <LoginModal isOpen={loginOpen} onClose={() => setLoginOpen(false)} />
                    <RegisterModal isOpen={registerOpen} onClose={() => setRegisterOpen(false)} />
                </div>
            </main>
        </div>
    );
}
