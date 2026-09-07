export const DEVELOPER_TYPE_META: Record<string, { emoji: string; label: string; tagline: string }> = {
  NIGHT_OWL: { emoji: "🌙", label: "NIGHT OWL", tagline: "새벽에 강한 개발자" },
  BUG_SLAYER: { emoji: "🔥", label: "BUG SLAYER", tagline: "버그를 사냥하는 개발자" },
  BUILDER: { emoji: "🏗", label: "BUILDER", tagline: "꾸준히 만들어가는 개발자" },
  POLYGLOT: { emoji: "🌐", label: "POLYGLOT", tagline: "여러 언어를 넘나드는 개발자" },
};

export function developerTypeMeta(type: string) {
  return DEVELOPER_TYPE_META[type] ?? { emoji: "✨", label: type, tagline: "고유한 개발 스타일을 가진 개발자" };
}

const WEEKDAYS = ["", "월", "화", "수", "목", "금", "토", "일"];

export function weekdayLabel(isoWeekday: number): string {
  return WEEKDAYS[isoWeekday] ?? "-";
}
