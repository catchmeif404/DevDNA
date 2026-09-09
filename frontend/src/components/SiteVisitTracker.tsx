"use client";

import { useEffect } from "react";
import { pingSiteVisit } from "@/lib/api";

export default function SiteVisitTracker() {
  useEffect(() => {
    pingSiteVisit();
  }, []);

  return null;
}
