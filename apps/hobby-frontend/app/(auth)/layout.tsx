import React from "react";
import { ErrorToastProvider } from "@/components/ui/ErrorToastProvider";

export default function AuthLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return <ErrorToastProvider>{children}</ErrorToastProvider>;
}
