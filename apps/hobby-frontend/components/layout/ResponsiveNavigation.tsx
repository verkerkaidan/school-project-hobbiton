"use client";

import { DesktopSidebar } from "./DesktopSidebar";
import { MobileBottomNav } from "./MobileBottomNav";

export const ResponsiveNavigation = () => {
  return (
    <>
      {/* Desktop */}
      <div className="hidden md:block">
        <DesktopSidebar />
      </div>

      {/* Mobile */}
      <div className="block md:hidden">
        <MobileBottomNav />
      </div>
    </>
  );
};
