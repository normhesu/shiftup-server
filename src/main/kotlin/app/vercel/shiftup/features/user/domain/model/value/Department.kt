@file:Suppress("MagicNumber")

package app.vercel.shiftup.features.user.domain.model.value

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(DepartmentSerializer::class)
sealed interface Department {
    val tenure: Tenure
    val symbol: String

    companion object {
        fun valueOf(value: String): Department = sequenceOf(TeuDepartment::valueOf, NeecDepartment::valueOf)
            .map { runCatching { it(value) } }
            .find { it.isSuccess }
            .let {
                requireNotNull(it) { "No enum constant Department.$it" }
            }
            .getOrThrow()
    }
}

@Serializable
enum class NeecDepartment(
    override val tenure: Tenure,
) : Department {
    // クリエイターズカレッジ
    // 放送芸術科
    B2(Tenure(2)),

    // 声優・演劇科
    T2(Tenure(2)),

    // マンガ・アニメーション科四年制
    LA(Tenure(4)),

    // マンガ・アニメーション科
    AN(Tenure(2)),

    // デザインカレッジ
    // ゲームクリエイター科四年制
    L4(Tenure(4)),

    // ゲームクリエイター科
    GM(Tenure(2)),

    // CG映像科
    CG(Tenure(3)),

    // デザイン科
    G2(Tenure(3)),

    // ミュージックカレッジ
    // ミュージックアーティスト科
    R2(Tenure(2)),

    // コンサート・イベント科
    F2(Tenure(2)),

    // 音響芸術科
    M2(Tenure(2)),

    // ITカレッジ
    // ITスペシャリスト科
    IS(Tenure(4)),

    // AIシステム科
    AI(Tenure(2)),

    // 情報処理科
    C2(Tenure(2)),

    // ネットワークセキュリティ科
    PN(Tenure(2)),

    // 情報ビジネス科
    L2(Tenure(2)),

    // テクノロジーカレッジ
    // ロボット科
    AR(Tenure(2)),

    // 電子・電気科
    E2(Tenure(2)),

    // 一級自動車整備科
    EV(Tenure(4)),

    // 自動車整備科
    RV(Tenure(2)),

    // 応用生物学科
    BN(Tenure(2)),

    // 建築学科
    X4(Tenure(4)),

    // 建築設計科
    X2(Tenure(2)),

    // 土木・造園科
    YZ(Tenure(2)),

    // 機械設計科
    DC(Tenure(2)),

    // スポーツ・医療カレッジ
    // スポーツトレーナー科三年制
    N3(Tenure(3)),

    // スポーツトレーナー科
    NA(Tenure(2)),

    // スポーツ健康学科三年制
    NE(Tenure(3)),

    // スポーツ健康学科
    N2(Tenure(2)),

    // 鍼灸科
    S3(Tenure(3)),

    // 柔道整復科
    J3(Tenure(3)),

    // 医療事務科
    MI(Tenure(2));

    override val symbol: String = name
}

@Serializable
enum class TeuDepartment : Department {
    BT, // 応用生物学部
    CS, // コンピュータサイエンス学部
    MS, // メディア学部
    ES, // 工学部
    DS, // デザイン学部
    HS; // 医療保健学部

    override val tenure = Tenure(4)
    override val symbol: String = name
}

object DepartmentSerializer : KSerializer<Department> {
    override val descriptor = PrimitiveSerialDescriptor(
        serialName = "DepartmentSerializer",
        kind = PrimitiveKind.STRING,
    )

    override fun serialize(encoder: Encoder, value: Department) {
        encoder.encodeString(value.symbol)
    }

    override fun deserialize(decoder: Decoder): Department {
        return Department.valueOf(decoder.decodeString())
    }
}
