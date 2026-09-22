package com.example.data.model

enum class UserRole(val thaiLabel: String, val description: String) {
    DRIVER("คนขับรถ", "ตรวจรถก่อนออก แจ้งเสีย ตรวจรับหลังซ่อม คืนรถสำรอง"),
    MECHANIC_HEAD("ช่างเต็ม (หัวหน้าช่าง)", "ประเมินอาการ บันทึกซ่อม อนุญาต/ห้ามใช้รถ ปล่อยรถ"),
    MECHANIC_FIELD("ช่างสวน (ช่างสนาม)", "ประเมินและซ่อมหน้างาน ทำแทนช่างเต็มได้"),
    DISPATCHER_TONG("โต้ง (ฝ่ายจัดคิว)", "ติดตามงาน จัดรถสำรอง ตรวจความครบถ้วน รับคืนรถ"),
    DISPATCHER_NGUYEN("เหวียน (ฝ่ายจัดคิว)", "ติดตามงาน จัดรถสำรอง ตรวจความครบถ้วน รับคืนรถ"),
    GATE_KOM("พี่ก้อม (ปล่อยรถ/เบี้ยเลี้ยง)", "ตรวจสอบความครบถ้วนและข้อจำกัดก่อนปล่อยรถและจ่ายเบี้ยเลี้ยง"),
    OWNER("เสี่ยสมศักดิ์ (เจ้าของ)", "รับแจ้งเหตุทางไกล ประสานอู่ภายนอก มอบหมายออฟฟิศ"),
    OFFICE("สุภา (ออฟฟิศ)", "บันทึกแทนจากการโทร (แยกผู้บันทึก/ผู้แจ้ง/ผู้อนุญาต)")
}

enum class VehicleType(val thaiLabel: String) {
    SEMI_MOTHER("รถหัวลาก (แม่)"),
    SEMI_TRAILER("หางบรรทุกน้ำมัน (ลูก)"),
    TEN_WHEEL("รถบรรทุกน้ำมันสิบล้อ"),
    SIX_WHEEL("รถบรรทุกน้ำมันหกล้อ")
}

enum class VehicleAuthStatus(val thaiLabel: String, val badgeColorType: String) {
    NOT_EVALUATED("ยังไม่ได้ประเมิน", "warning"),
    CONDITIONAL_USE("ใช้งานได้ตามเงื่อนไข", "info"),
    PROHIBITED("ห้ามใช้งาน", "danger"),
    GARAGE_TRANSIT_ONLY("อนุญาตเฉพาะเดินทางไปอู่", "warning")
}

enum class WorkOrderStatus(val thaiLabel: String, val stepIndex: Int) {
    WAITING_INSPECTION("รอตรวจ", 1),
    INSPECTED_PENDING("ตรวจแล้วรอดำเนินการ", 2),
    IN_REPAIR("กำลังซ่อม", 3),
    WAITING_PARTS("รออะไหล่", 3),
    SENT_EXTERNAL("ส่งอู่ภายนอก", 3),
    REPAIRED_WAITING_DRIVER("ซ่อมเสร็จรอคนขับตรวจรับ", 4),
    DRIVER_ACCEPTED_WAITING_MECHANIC("คนขับตรวจรับแล้วรอช่างยืนยัน", 5),
    COMPLETED("จบงาน (ปล่อยรถได้)", 6)
}

enum class CheckResult(val thaiLabel: String) {
    NORMAL("ปกติ"),
    ISSUE("พบปัญหา"),
    MECHANIC_CHECK("ให้ช่างช่วยตรวจ")
}
