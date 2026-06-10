import React from "react";

interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  children: React.ReactNode;
  largeModal?: boolean;
}

export function Modal({
                        isOpen,
                        onClose,
                        children,
                        largeModal = false,
                      }: ModalProps) {
  const mouseDownTarget = React.useRef<EventTarget | null>(null);

  if (!isOpen) return null;
  return (
      <div
          className="fixed inset-0 bg-black/70 backdrop-blur-sm flex items-center justify-center z-50 p-2 sm:p-4"
          onMouseDown={(e) => {
            mouseDownTarget.current = e.target;
          }}
          onClick={(e) => {
            if (mouseDownTarget.current === e.currentTarget) onClose();
          }}
      >
        <div
            className={`
          w-full 
          ${largeModal ? "md:max-w-5xl" : "md:max-w-xl"} 
          bg-background 
          border border-white/10 
          rounded-xl 
          p-5 
          md:p-8 
          shadow-2xl 
          max-h-[92vh] 
          md:max-h-[85vh] 
          overflow-y-auto
        `}
        >
          {children}
        </div>
      </div>
  );
}