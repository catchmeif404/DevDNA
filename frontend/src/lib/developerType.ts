export type DeveloperTypeLocale = "ko" | "en";

type TypeRecord = {
  code: string;
  label: string;
  en: { tagline: string; description: string };
  ko: { tagline: string; description: string };
};

// Type IDs stay aligned with the scorer; only public-facing case-file names change.
const TYPES: Record<string, TypeRecord> = {
  "NIGHT_OWL": {
    "code": "B-01",
    "label": "AFTER-HOURS OPERATOR",
    "en": {
      "tagline": "Last seen after midnight.",
      "description": "A large share of commits was recorded between midnight and 6 a.m."
    },
    "ko": {
      "tagline": "마지막 목격 시각, 자정 이후.",
      "description": "자정부터 오전 6시 사이에 커밋이 집중된 흔적이 확인됩니다."
    }
  },
  "BUG_SLAYER": {
    "code": "B-02",
    "label": "BUG HUNTER",
    "en": {
      "tagline": "The bugs never stood a chance.",
      "description": "Fix commits account for an unusually large share of the evidence."
    },
    "ko": {
      "tagline": "버그의 행방은 여기서 끊깁니다.",
      "description": "수집된 커밋 중 버그 수정 기록의 비율이 높습니다."
    }
  },
  "BUILDER": {
    "code": "B-03",
    "label": "SERIAL BUILDER",
    "en": {
      "tagline": "Another feature. Same suspect.",
      "description": "Feature commits and sustained activity point to a repeat builder."
    },
    "ko": {
      "tagline": "또 새 기능. 대상은 동일.",
      "description": "새 기능 추가 비율과 지속적인 활동 기록이 확인됩니다."
    }
  },
  "POLYGLOT": {
    "code": "B-04",
    "label": "MULTILINGUAL OPERATOR",
    "en": {
      "tagline": "Several languages. One signature.",
      "description": "Multiple programming languages appear across the activity record."
    },
    "ko": {
      "tagline": "여러 언어, 하나의 서명.",
      "description": "여러 프로그래밍 언어를 넘나든 활동 흔적이 확인됩니다."
    }
  },
  "WEEKEND_WARRIOR": {
    "code": "B-05",
    "label": "WEEKEND OPERATIVE",
    "en": {
      "tagline": "No alibi for the weekend.",
      "description": "Weekend commits match or exceed the weekday trail."
    },
    "ko": {
      "tagline": "주말 알리바이가 없습니다.",
      "description": "평일 못지않게, 혹은 그 이상으로 주말 커밋이 이어집니다."
    }
  },
  "REFACTOR_MASTER": {
    "code": "B-06",
    "label": "CODE RESTORER",
    "en": {
      "tagline": "Left the scene cleaner.",
      "description": "Refactoring and structural improvements recur throughout the record."
    },
    "ko": {
      "tagline": "현장이 더 깔끔해졌습니다.",
      "description": "코드 정리와 구조 개선 기록이 반복해서 발견됩니다."
    }
  },
  "DOCUMENTARIAN": {
    "code": "B-07",
    "label": "ARCHIVIST",
    "en": {
      "tagline": "Everything was documented.",
      "description": "Documentation commits make up a notable share of the paper trail."
    },
    "ko": {
      "tagline": "전부 기록되어 있었습니다.",
      "description": "문서 작성 커밋이 활동 기록의 상당 부분을 차지합니다."
    }
  },
  "TESTER": {
    "code": "B-08",
    "label": "QUALITY INSPECTOR",
    "en": {
      "tagline": "Trust requires evidence.",
      "description": "Test commits repeatedly appear alongside development activity."
    },
    "ko": {
      "tagline": "신뢰에는 증거가 필요합니다.",
      "description": "테스트 작성으로 검증한 흔적이 자주 발견됩니다."
    }
  },
  "EXPLORER": {
    "code": "B-09",
    "label": "REPO EXPLORER",
    "en": {
      "tagline": "Sightings in multiple locations.",
      "description": "Activity spans an unusually large number of repositories."
    },
    "ko": {
      "tagline": "여러 장소에서 목격됩니다.",
      "description": "유난히 많은 저장소에서 활동한 기록이 확인됩니다."
    }
  },
  "COLLABORATOR": {
    "code": "B-10",
    "label": "COLLABORATOR",
    "en": {
      "tagline": "Did not work alone.",
      "description": "Pull requests leave a clear trail of collaboration."
    },
    "ko": {
      "tagline": "단독 작업이 아니었습니다.",
      "description": "풀 리퀘스트를 통한 공조 활동이 뚜렷하게 드러납니다."
    }
  }
};

export function developerTypeMeta(type: string, locale: DeveloperTypeLocale) {
  const record = TYPES[type];
  if (record) return { code: record.code, label: record.label, ...record[locale] };
  return {
    code: "B-00",
    label: "UNCLASSIFIED",
    tagline: locale === "ko" ? "아직 분류되지 않은 흔적." : "A trail of its own.",
    description: locale === "ko"
      ? "현재 유형만으로는 뚜렷하게 분류할 수 없는 활동 기록입니다."
      : "The activity on file does not clearly match an established profile.",
  };
}
