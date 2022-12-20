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
    val japaneseName: String
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
    override val japaneseName: String,
    override val tenure: Tenure,
) : Department {
    // クリエイターズカレッジ
    B2(japaneseName = "放送芸術科", Tenure(2)),
    T2(japaneseName = "声優・演劇科", Tenure(2)),
    LA(japaneseName = "マンガ・アニメーション科四年制", Tenure(4)),
    AN(japaneseName = "マンガ・アニメーション科", Tenure(2)),

    // デザインカレッジ
    L4(japaneseName = "ゲームクリエイター科四年制", Tenure(4)),
    GM(japaneseName = "ゲームクリエイター科", Tenure(2)),
    CG(japaneseName = "CG映像科", Tenure(3)),
    G2(japaneseName = "デザイン科", Tenure(3)),

    // 　ミュージックカレッジ
    R2(japaneseName = "ミュージックアーティスト科", Tenure(2)),
    F2(japaneseName = "コンサート・イベント科", Tenure(2)),
    M2(japaneseName = "音響芸術科", Tenure(2)),

    // 　ITカレッジ
    IS(japaneseName = "ITスペシャリスト科", Tenure(4)),
    AI(japaneseName = "AIシステム科", Tenure(2)),
    C2(japaneseName = "情報処理科", Tenure(2)),
    PN(japaneseName = "ネットワークセキュリティ科", Tenure(2)),
    L2(japaneseName = "情報ビジネス科", Tenure(2)),

    // テクノロジーカレッジ
    AR(japaneseName = "ロボット科", Tenure(2)),
    E2(japaneseName = "電子・電気科", Tenure(2)),
    EV(japaneseName = "一級自動車整備科", Tenure(4)),
    RV(japaneseName = "自動車整備科", Tenure(2)),
    BN(japaneseName = "応用生物学科", Tenure(2)),
    X4(japaneseName = "建築学科", Tenure(4)),
    X2(japaneseName = "建築設計科", Tenure(2)),
    YZ(japaneseName = "土木・造園科", Tenure(2)),
    DC(japaneseName = "機械設計科", Tenure(2)),

    // スポーツ・医療カレッジ
    N3(japaneseName = "スポーツトレーナー科三年制", Tenure(3)),
    NA(japaneseName = "スポーツトレーナー科", Tenure(2)),
    NE(japaneseName = "スポーツ健康学科三年制", Tenure(3)),
    N2(japaneseName = "スポーツ健康学科", Tenure(2)),
    S3(japaneseName = "鍼灸科", Tenure(3)),
    J3(japaneseName = "柔道整復科", Tenure(3)),
    MI(japaneseName = "医療事務科", Tenure(2));

    override val symbol: String = name
}

@Serializable
enum class TeuDepartment(
    override val japaneseName: String,
) : Department {
    BT("応用生物学部"),
    CS("コンピュータサイエンス学部"),
    MS("メディア学部"),
    ES("工学部"),
    DS("デザイン学部"),
    HS("医療保健学部");

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
