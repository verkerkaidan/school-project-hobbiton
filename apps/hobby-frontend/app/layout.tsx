import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";
import React from "react";
import { Sidebar } from "@/components/layout/Sidebar";
import { NotificationProvider } from "@/components/notifications/NotificationProvider";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "Hobbithon",
  description: "Hobby Social Media App",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" className="dark">
      <body
        className={`${geistSans.variable} ${geistMono.variable} antialiased bg-background text-stone-200 min-h-screen`}
      >
        <div className="flex min-h-screen">
          <NotificationProvider>
            <Sidebar />
            <main className="flex-1 ml-20 md:ml-64">{children}</main>
          </NotificationProvider>
        </div>
      </body>
    </html>
  );
}
