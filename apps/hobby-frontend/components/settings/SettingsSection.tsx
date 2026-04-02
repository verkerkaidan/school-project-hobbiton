import { ChevronRight } from "lucide-react";

interface SettingsSectionProps {
  icon: React.ReactNode;
  title: string;
  description: string;
  children?: React.ReactNode;
}

export function SettingsSection({
  icon,
  title,
  description,
  children,
}: SettingsSectionProps) {
  return (
    <div className="glass-card rounded-2xl p-6 flex flex-col gap-4">
      <div className="flex items-center gap-3">
        <div className="text-brand">{icon}</div>
        <div>
          <h2 className="font-semibold text-base">{title}</h2>
          <p className="text-accent text-sm">{description}</p>
        </div>
        {!children && (
          <ChevronRight className="ml-auto text-accent opacity-40" size={18} />
        )}
      </div>
      {children}
    </div>
  );
}
