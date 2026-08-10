package com.example.data.model

enum class AppRole {
    CUSTOMER,
    DRIVER,
    ADMIN
}

enum class VehicleType(
    val title: String,
    val capacity: String,
    val baseFare: Double,
    val perKmRate: Double,
    val iconName: String,
    val description: String
) {
    TWO_WHEELER("2-Wheeler Courier", "Up to 20 kg", 40.0, 10.0, "two_wheeler", "Quick documents & small parcels"),
    THREE_WHEELER("3-Wheeler Auto", "Up to 500 kg", 120.0, 18.0, "electric_rickshaw", "Plywood, small crates & hardware"),
    TATA_ACE("Tata Ace / Chota Hathi", "Up to 750 kg", 250.0, 25.0, "local_shipping", "Household shifting & heavy cargo"),
    PICKUP_8FT("Pickup 8ft", "Up to 1200 kg", 400.0, 32.0, "fire_truck", "Peenya industrial machinery & bulk goods")
}

enum class GoodsCategory(val label: String) {
    ELECTRICAL_HARDWARE("Electricals & Hardware"),
    HOUSEHOLD_FURNITURE("Household & Furniture"),
    WHOLESALE_FMCG("Wholesale & FMCG Goods"),
    INDUSTRIAL_MACHINERY("Industrial Machinery"),
    DOCUMENTS_PARCELS("Documents & Small Parcels"),
    BUILDING_MATERIALS("Building & Tiles")
}

enum class KYCStatus {
    NOT_SUBMITTED,
    PENDING_APPROVAL,
    APPROVED,
    REJECTED
}

enum class BookingStatus(val displayLabel: String) {
    SEARCHING_DRIVER("Searching Nearby Driver"),
    ACCEPTED("Driver Assigned & On the Way"),
    ARRIVED_PICKUP("Driver Arrived at Pickup Point"),
    GOODS_LOADED("Goods Loaded & In Transit"),
    DELIVERED_CASH_COLLECTED("Delivered - Cash Collected")
}

enum class WalletTxType {
    REGISTRATION_FEE,
    DAILY_DEDUCTION,
    TOPUP,
    INCENTIVE
}
