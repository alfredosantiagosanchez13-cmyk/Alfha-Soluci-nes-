package com.example.data.model

enum class DeviceType(val label: String, val iconEmoji: String) {
    LIGHT("Iluminación", "💡"),
    CLIMATE("Climatización / HVAC", "❄️"),
    GATEWAY("Conmutador / Gateway", "🔌"),
    CURTAIN("Persiana / Toldo", "🪟"),
    LOCK("Cerradura / Acceso", "🔒")
}

enum class CommunicationProtocol(val label: String, val badge: String, val colorHex: Long) {
    REST_API("REST API (WiFi/LAN)", "HTTP/REST", 0xFF06B6D4),
    BLUETOOTH_LE("Bluetooth LE (GATT)", "BLE 5.0", 0xFF8B5CF6)
}

enum class ClimateHvacMode(val label: String, val iconEmoji: String, val colorHex: Long) {
    COOL("Frío (Cool)", "❄️", 0xFF38BDF8),
    HEAT("Calor (Heat)", "☀️", 0xFFF97316),
    ECO("Ecológico (Eco)", "🍃", 0xFF10B981),
    AUTO("Automático", "⚡", 0xFFA855F7),
    FAN_ONLY("Ventilación", "🌀", 0xFF94A3B8),
    OFF("Apagado", "⭕", 0xFF64748B)
}

enum class FanSpeed(val label: String) {
    AUTO("Auto"),
    LOW("Baja"),
    MED("Media"),
    HIGH("Alta")
}

data class SmartScenePreset(
    val id: String,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val lightsOn: Boolean,
    val targetBrightness: Int,
    val targetColorHex: String,
    val targetTempC: Float,
    val hvacMode: ClimateHvacMode,
    val fanSpeed: FanSpeed
)

data class DiscoveredIotDevice(
    val deviceId: String,
    val name: String,
    val zone: String,
    val type: DeviceType,
    val protocol: CommunicationProtocol,
    val endpointOrAddress: String,
    val rssi: Int = -60,
    val isAlreadyAdded: Boolean = false
)

data class SmartHomeCommandResult(
    val success: Boolean,
    val actionSummary: String,
    val targetedDevicesCount: Int,
    val details: String,
    val protocolUsed: CommunicationProtocol? = null
)
