import type { Metadata } from "next";
import type { ReactNode } from "react";
import "./globals.css";

export const metadata: Metadata = {
  title: "Warrior · GitHub Connection",
  description: "Next.js dashboard connected to the KhodeSushianm/Warrior GitHub repository.",
};

export default function RootLayout({ children }: { children: ReactNode }) {
  return (
    <html lang="en">
      <body className="bg-[#0b1020] text-slate-100 antialiased">{children}</body>
    </html>
  );
}
