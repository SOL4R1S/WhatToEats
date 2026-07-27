"use client";

import { createContext, useContext, useState, ReactNode } from "react";

interface SidebarState {
  left: ReactNode;
  right: ReactNode;
}

interface SidebarContextType {
  sidebars: SidebarState;
  setLeftSidebar: (node: ReactNode) => void;
  setRightSidebar: (node: ReactNode) => void;
}

const SidebarContext = createContext<SidebarContextType | null>(null);

export function SidebarProvider({ children }: { children: ReactNode }) {
  const [sidebars, setSidebars] = useState<SidebarState>({
    left: null,
    right: null,
  });

  const setLeftSidebar = (node: ReactNode) =>
    setSidebars((prev) => ({ ...prev, left: node }));
  const setRightSidebar = (node: ReactNode) =>
    setSidebars((prev) => ({ ...prev, right: node }));

  return (
    <SidebarContext.Provider
      value={{ sidebars, setLeftSidebar, setRightSidebar }}
    >
      {children}
    </SidebarContext.Provider>
  );
}

export function useSidebar() {
  const ctx = useContext(SidebarContext);
  if (!ctx) {
    throw new Error("useSidebar must be used within SidebarProvider");
  }
  return ctx;
}

// Convenience hook alias for pages that only need the right sidebar setter
export function useRightSidebar() {
  const { setRightSidebar } = useSidebar();
  return { setRightSidebar };
}
