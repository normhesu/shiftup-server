package app.vercel.shiftup.features.user.domain.model.value

enum class Department(
    val japaneseName: String,
    val tenure: Int,
) {
    // クリエイターズカレッジ
    B2(japaneseName = "放送芸術科", tenure = 2),
    T2(japaneseName = "声優・演劇科", tenure = 2),
    LA(japaneseName = "マンガ・アニメーション科四年制", tenure = 4),
    AN(japaneseName = "マンガ・アニメーション科", tenure = 2),

    // デザインカレッジ
    L4(japaneseName = "ゲームクリエイター科四年制", tenure = 4),
    GM(japaneseName = "ゲームクリエイター科", tenure = 2),
    CG(japaneseName = "CG映像科", tenure = 3),
    G2(japaneseName = "デザイン科", tenure = 3),

    // 　ミュージックカレッジ
    R2(japaneseName = "ミュージックアーティスト科", tenure = 2),
    F2(japaneseName = "コンサート・イベント科", tenure = 2),
    M2(japaneseName = "音響芸術科", tenure = 2),

    // 　ITカレッジ
    IS(japaneseName = "ITスペシャリスト科", tenure = 4),
    AI(japaneseName = "AIシステム科", tenure = 2),
    C2(japaneseName = "情報処理科", tenure = 2),
    PN(japaneseName = "ネットワークセキュリティ科", tenure = 2),
    L2(japaneseName = "情報ビジネス科", tenure = 2),

    // テクノロジーカレッジ
    AR(japaneseName = "ロボット科", tenure = 2),
    E2(japaneseName = "電子・電気科", tenure = 2),
    EV(japaneseName = "一級自動車整備科", tenure = 4),
    RV(japaneseName = "自動車整備科", tenure = 2),
    BN(japaneseName = "応用生物学科", tenure = 2),
    X4(japaneseName = "建築学科", tenure = 4),
    X2(japaneseName = "建築設計科", tenure = 2),
    YZ(japaneseName = "土木・造園科", tenure = 2),
    DC(japaneseName = "機械設計科", tenure = 2),

    // スポーツ・医療カレッジ
    N3(japaneseName = "スポーツトレーナー科三年制", tenure = 3),
    NA(japaneseName = "スポーツトレーナー科", tenure = 2),
    NE(japaneseName = "スポーツ健康学科三年制", tenure = 3),
    N2(japaneseName = "スポーツ健康学科", tenure = 2),
    S3(japaneseName = "鍼灸科", tenure = 3),
    J3(japaneseName = "柔道整復科", tenure = 3),
    Mi(japaneseName = "医療事務科", tenure = 2);

    val symbol: String = name
}
