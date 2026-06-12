"use client";

import { useEffect, useState } from "react";

export function useTheme() {
  const [isDarkMode, setIsDarkMode] = useState(true);

  useEffect(() => {
    const saved = localStorage.getItem("theme");
    const isDark = saved ? saved === "dark" : true;
    setIsDarkMode(isDark);
    applyTheme(isDark);
  }, []);

  const applyTheme = (isDark: boolean) => {
    const html = document.documentElement;

    if (isDark) {
      html.classList.remove("light-mode");
      localStorage.setItem("theme", "dark");
    } else {
      html.classList.add("light-mode");
      localStorage.setItem("theme", "light");
    }
  };

  const toggleTheme = () => {
    const next = !isDarkMode;
    setIsDarkMode(next);
    applyTheme(next);
  };

  return { isDarkMode, toggleTheme };
}
