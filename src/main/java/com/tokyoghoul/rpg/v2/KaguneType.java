package com.tokyoghoul.rpg.v2;

/**
 * Eight playable canonical character kagune profiles.
 * These are character-specific kagune profiles, not eight new RC types.
 */
public enum KaguneType {
    NONE("Нет", "Человек / CCG", BaseType.NONE),

    KANEKI_RINKAKU("Канэки — Ринкаку", "Ринкаку Канэки • гибкие щупальца", BaseType.RINKAKU),
    TAKIZAWA_UKAKU("Такизава — Укаку", "Укаку Сэйдо • крыловидная форма", BaseType.UKAKU),

    RIZE_RINKAKU("Ризе — Ринкаку", "Ринкаку Ризе • хищные гибкие отростки", BaseType.RINKAKU),
    TOUKA_UKAKU("Тоука — Укаку", "Укаку Тоуки • крылья и RC-снаряды", BaseType.UKAKU),
    AYATO_UKAKU("Аято — Укаку", "Укаку Аято • крылья и кристаллизованные лезвия", BaseType.UKAKU),
    TSUKIYAMA_KOUKAKU("Цукияма — Коукаку", "Коукаку Шу • плотная форма", BaseType.KOUKAKU),
    NISHIKI_BIKAKU("Нишики — Бикаку", "Бикаку Нишики • хвостовая форма", BaseType.BIKAKU),
    NORO_BIKAKU("Норо — особая форма", "Массивная форма Норо; точный RC-тип не указываю из-за противоречивых описаний", BaseType.NONE);

    public enum BaseType { NONE, RINKAKU, UKAKU, KOUKAKU, BIKAKU }

    private final String displayName;
    private final String description;
    private final BaseType baseType;

    KaguneType(String displayName, String description, BaseType baseType) {
        this.displayName = displayName;
        this.description = description;
        this.baseType = baseType;
    }
    public String displayName() { return displayName; }
    public String description() { return description; }
    public BaseType baseType() { return baseType; }
    public boolean isRinkaku() { return baseType == BaseType.RINKAKU; }
    public boolean isUkaku() { return baseType == BaseType.UKAKU; }
    public boolean isKoukaku() { return baseType == BaseType.KOUKAKU; }
    public boolean isBikaku() { return baseType == BaseType.BIKAKU; }
}
