export type DeveloperTypeLocale = "ko" | "en";

const EMOJI_LABEL: Record<string, { emoji: string; label: string }> = {
  NIGHT_OWL: { emoji: "🦉", label: "NIGHT OWL" },
  BUG_SLAYER: { emoji: "🐱", label: "DEBUG CAT" },
  BUILDER: { emoji: "🦫", label: "BUILDER BEAVER" },
  POLYGLOT: { emoji: "🦜", label: "POLYGLOT PARROT" },
  WEEKEND_WARRIOR: { emoji: "🦦", label: "WEEKEND OTTER" },
  REFACTOR_MASTER: { emoji: "🦊", label: "TIDY FOX" },
  DOCUMENTARIAN: { emoji: "🐘", label: "ARCHIVIST ELEPHANT" },
  TESTER: { emoji: "🐭", label: "LAB MOUSE" },
  EXPLORER: { emoji: "🐢", label: "EXPLORER TURTLE" },
  COLLABORATOR: { emoji: "🐧", label: "TEAM PENGUIN" },
};

// Labels/emoji are locale-independent (same badge/card text either way) -- only the
// tagline/description prose needs a translation per locale.
const TAGLINE_DESCRIPTION: Record<DeveloperTypeLocale, Record<string, { tagline: string; description: string }>> = {
  ko: {
    NIGHT_OWL: {
      tagline: "새벽에 강한 개발자",
      description: "커밋의 상당수가 자정부터 새벽 6시 사이에 몰려 있어요.",
    },
    BUG_SLAYER: {
      tagline: "버그를 사냥하는 개발자",
      description: "버그 수정(fix) 커밋 비율이 유난히 높아요.",
    },
    BUILDER: {
      tagline: "꾸준히 만들어가는 개발자",
      description: "새 기능(feature) 커밋 비율이 높고, 오랜 기간 꾸준히 활동해요.",
    },
    POLYGLOT: {
      tagline: "여러 언어를 넘나드는 개발자",
      description: "여러 프로그래밍 언어를 넘나들며 작업해요.",
    },
    WEEKEND_WARRIOR: {
      tagline: "주말에 불타오르는 개발자",
      description: "평일 못지않게, 혹은 그 이상으로 주말에도 커밋이 이어져요.",
    },
    REFACTOR_MASTER: {
      tagline: "코드를 갈고 닦는 개발자",
      description: "코드 정리와 구조 개선(refactor) 커밋 비율이 높아요.",
    },
    DOCUMENTARIAN: {
      tagline: "기록을 남기는 개발자",
      description: "문서(docs) 커밋 비율이 높아요 — 기록을 꼼꼼히 남기는 편이에요.",
    },
    TESTER: {
      tagline: "테스트로 증명하는 개발자",
      description: "테스트 커밋 비율이 높아요 — 안정성을 코드로 챙기는 편이에요.",
    },
    EXPLORER: {
      tagline: "여러 저장소를 넘나드는 개발자",
      description: "특정 프로젝트 하나에 머물지 않고, 유난히 많은 저장소를 넘나들며 활동해요.",
    },
    COLLABORATOR: {
      tagline: "협업으로 성장하는 개발자",
      description: "Pull Request를 통한 협업 활동이 활발해요.",
    },
  },
  en: {
    NIGHT_OWL: {
      tagline: "Strongest after midnight",
      description: "A large share of your commits land between midnight and 6am.",
    },
    BUG_SLAYER: {
      tagline: "Hunts bugs for sport",
      description: "An unusually high share of your commits are fixes.",
    },
    BUILDER: {
      tagline: "Keeps building, steadily",
      description: "High share of feature commits, sustained over a long stretch of time.",
    },
    POLYGLOT: {
      tagline: "At home in many languages",
      description: "You move across a wide range of programming languages.",
    },
    WEEKEND_WARRIOR: {
      tagline: "Fired up on weekends",
      description: "Commits keep coming on weekends, as much as (or more than) weekdays.",
    },
    REFACTOR_MASTER: {
      tagline: "Polishes the code",
      description: "High share of your commits are refactors -- cleanup and structure work.",
    },
    DOCUMENTARIAN: {
      tagline: "Leaves a paper trail",
      description: "High share of docs commits -- you write things down carefully.",
    },
    TESTER: {
      tagline: "Proves it with tests",
      description: "High share of test commits -- you back up stability with code.",
    },
    EXPLORER: {
      tagline: "Roams across repos",
      description: "You don't stay in one project -- you're active across an unusually wide set of repos.",
    },
    COLLABORATOR: {
      tagline: "Grows through collaboration",
      description: "Pull-request-driven collaboration is a big part of how you work.",
    },
  },
};

const FALLBACK: Record<DeveloperTypeLocale, { tagline: string; description: string }> = {
  ko: {
    tagline: "고유한 개발 스타일을 가진 개발자",
    description: "다른 10가지 유형 어디에도 뚜렷하게 속하지 않는, 고유한 활동 패턴을 가지고 있어요.",
  },
  en: {
    tagline: "A developer with a style of their own",
    description: "Doesn't clearly fit any of the other 10 types -- a genuinely unique activity pattern.",
  },
};

export function developerTypeMeta(type: string, locale: DeveloperTypeLocale) {
  const base = EMOJI_LABEL[type] ?? { emoji: "✨", label: type };
  const localized = TAGLINE_DESCRIPTION[locale][type] ?? FALLBACK[locale];
  return { ...base, ...localized };
}
