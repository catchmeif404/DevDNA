export const DEVELOPER_TYPE_META: Record<
  string,
  { emoji: string; label: string; tagline: string; description: string }
> = {
  NIGHT_OWL: {
    emoji: "🦉",
    label: "NIGHT OWL",
    tagline: "새벽에 강한 개발자",
    description: "커밋의 상당수가 자정부터 새벽 6시 사이에 몰려 있어요.",
  },
  BUG_SLAYER: {
    emoji: "🐱",
    label: "DEBUG CAT",
    tagline: "버그를 사냥하는 개발자",
    description: "버그 수정(fix) 커밋 비율이 유난히 높아요.",
  },
  BUILDER: {
    emoji: "🦫",
    label: "BUILDER BEAVER",
    tagline: "꾸준히 만들어가는 개발자",
    description: "새 기능(feature) 커밋 비율이 높고, 오랜 기간 꾸준히 활동해요.",
  },
  POLYGLOT: {
    emoji: "🦜",
    label: "POLYGLOT PARROT",
    tagline: "여러 언어를 넘나드는 개발자",
    description: "여러 프로그래밍 언어를 넘나들며 작업해요.",
  },
  WEEKEND_WARRIOR: {
    emoji: "🦦",
    label: "WEEKEND OTTER",
    tagline: "주말에 불타오르는 개발자",
    description: "평일 못지않게, 혹은 그 이상으로 주말에도 커밋이 이어져요.",
  },
  REFACTOR_MASTER: {
    emoji: "🦊",
    label: "TIDY FOX",
    tagline: "코드를 갈고 닦는 개발자",
    description: "코드 정리와 구조 개선(refactor) 커밋 비율이 높아요.",
  },
  DOCUMENTARIAN: {
    emoji: "🐘",
    label: "ARCHIVIST ELEPHANT",
    tagline: "기록을 남기는 개발자",
    description: "문서(docs) 커밋 비율이 높아요 — 기록을 꼼꼼히 남기는 편이에요.",
  },
  TESTER: {
    emoji: "🐭",
    label: "LAB MOUSE",
    tagline: "테스트로 증명하는 개발자",
    description: "테스트 커밋 비율이 높아요 — 안정성을 코드로 챙기는 편이에요.",
  },
  EXPLORER: {
    emoji: "🐢",
    label: "EXPLORER TURTLE",
    tagline: "여러 저장소를 넘나드는 개발자",
    description: "특정 프로젝트 하나에 머물지 않고, 유난히 많은 저장소를 넘나들며 활동해요.",
  },
  COLLABORATOR: {
    emoji: "🐧",
    label: "TEAM PENGUIN",
    tagline: "협업으로 성장하는 개발자",
    description: "Pull Request를 통한 협업 활동이 활발해요.",
  },
};

export function developerTypeMeta(type: string) {
  return (
    DEVELOPER_TYPE_META[type] ?? {
      emoji: "✨",
      label: type,
      tagline: "고유한 개발 스타일을 가진 개발자",
      description: "다른 10가지 유형 어디에도 뚜렷하게 속하지 않는, 고유한 활동 패턴을 가지고 있어요.",
    }
  );
}

const WEEKDAYS = ["", "월", "화", "수", "목", "금", "토", "일"];

export function weekdayLabel(isoWeekday: number): string {
  return WEEKDAYS[isoWeekday] ?? "-";
}
