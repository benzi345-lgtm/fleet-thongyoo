package com.example.data.model

import java.util.UUID

object InspectionTemplate {

    data class TemplateItemDef(
        val categoryId: Int,
        val categoryName: String,
        val itemKey: String,
        val itemTitle: String,
        val forVehicleType: String? = null, // null for all, or SEMI_MOTHER, SEMI_TRAILER
        val inspectionMethod: String? = null
    )

    val ALL_DEFINITIONS = listOf(
        // หมวด 1: ยางและล้อ
        TemplateItemDef(1, "1. ยางและล้อ", "tire_damage", "ความเสียหายยางที่มองเห็น (บวม ฉีก แตก)", inspectionMethod = "ตรวจสายตารอบยางทุกเส้น"),
        TemplateItemDef(1, "1. ยางและล้อ", "tire_pressure_deflation", "อาการยุบหรือแบนผิดปกติของยาง", inspectionMethod = "สังเกตการยุบตัวของแก้มยาง"),
        TemplateItemDef(1, "1. ยางและล้อ", "wheel_lugs", "ความเสียหายกระทะล้อและน็อตล้อทุกตัว", inspectionMethod = "ตรวจน็อตล้อครบแน่น ไม่มีสนิมไหล"),

        // หมวด 2: เบรกและระบบลม
        TemplateItemDef(2, "2. เบรกและระบบลม", "brake_warning", "ไฟหรือเสียงเตือนระบบเบรกหน้าปัด", inspectionMethod = "สังเกตไฟเตือนตอนเปิดสวิตช์กุญแจ"),
        TemplateItemDef(2, "2. เบรกและระบบลม", "air_gauge", "มาตรวัดลมขึ้นตามเกณฑ์ช่าง (อย่างน้อย 7-8 บาร์)", inspectionMethod = "ดูเข็มเกจ์ลมทั้ง 2 วง"),
        TemplateItemDef(2, "2. เบรกและระบบลม", "air_leak_sound", "เสียงลมรั่ว/สายลมรั่วในจุดที่ตรวจได้ปลอดภัย", inspectionMethod = "ดับเครื่องเหยียบเบรกฟังเสียงลมรั่ว"),
        TemplateItemDef(2, "2. เบรกและระบบลม", "service_brake", "เบรกเท้าทำงานตามวิธีที่ช่างสอน", inspectionMethod = "ทดสอบเหยียบเบรกเท้าขณะออกตัวช้า"),
        TemplateItemDef(2, "2. เบรกและระบบลม", "parking_brake", "เบรกจอด (เบรกลมดึง) ทำงานตามวิธีที่ช่างสอน", inspectionMethod = "ดึงเบรกลมทดสอบการหยุดของรถ"),

        // หมวด 3: ไฟและสัญญาณ
        TemplateItemDef(3, "3. ไฟและสัญญาณ", "headlights", "ไฟหน้า (ไฟต่ำ / ไฟสูง)", inspectionMethod = "เปิดสวิตช์ตรวจดูหน้ารถ"),
        TemplateItemDef(3, "3. ไฟและสัญญาณ", "taillights", "ไฟท้ายและไฟส่องป้ายทะเบียน", inspectionMethod = "เดินตรวจท้ายรถ"),
        TemplateItemDef(3, "3. ไฟและสัญญาณ", "brake_lights", "ไฟเบรก (สว่างชัดเจนทั้งสองข้าง)", inspectionMethod = "เหยียบเบรกสังเกตการสะท้อนหรือให้คนช่วยดู"),
        TemplateItemDef(3, "3. ไฟและสัญญาณ", "turn_signals", "ไฟเลี้ยวซ้ายและขวา", inspectionMethod = "เปิดไฟเลี้ยวตรวจรอบคัน"),
        TemplateItemDef(3, "3. ไฟและสัญญาณ", "hazard_lights", "ไฟฉุกเฉิน (ไฟผ่าหมาก)", inspectionMethod = "เปิดไฟฉุกเฉินไฟต้องติดพร้อมกัน"),
        TemplateItemDef(3, "3. ไฟและสัญญาณ", "horn", "เสียงแตรดังชัดเจน", inspectionMethod = "กดแตรทดสอบเสียง"),
        TemplateItemDef(3, "3. ไฟและสัญญาณ", "marker_reflectors", "ไฟตำแหน่งและแผ่นสะท้อนแสงรอบคัน", inspectionMethod = "ตรวจความสะอาดและติดครบ"),

        // หมวด 4: ทัศนวิสัยและห้องขับ
        TemplateItemDef(4, "4. ทัศนวิสัยและห้องขับ", "windshield", "กระจกหน้าใส ไม่มีรอยร้าวขวางตา", inspectionMethod = "ตรวจความสะอาดและรอยแตกร้าว"),
        TemplateItemDef(4, "4. ทัศนวิสัยและห้องขับ", "side_mirrors", "กระจกมองข้างซ้าย–ขวา ปรับมุมมองชัดเจน", inspectionMethod = "ปรับตำแหน่งมองเห็นมุมบอด"),
        TemplateItemDef(4, "4. ทัศนวิสัยและห้องขับ", "wipers", "ใบปัดน้ำฝนไม่ฉีกขาดและกวาดน้ำได้ดี", inspectionMethod = "เปิดทดสอบระบบปัด"),
        TemplateItemDef(4, "4. ทัศนวิสัยและห้องขับ", "washer_fluid", "ระบบฉีดน้ำล้างกระจกทำงานและน้ำเต็ม", inspectionMethod = "กดฉีดน้ำล้างกระจก"),
        TemplateItemDef(4, "4. ทัศนวิสัยและห้องขับ", "seatbelts", "เข็มขัดนิรภัยและตัวล็อกทำงานสมบูรณ์", inspectionMethod = "ดึงกระตุกทดสอบการล็อก"),
        TemplateItemDef(4, "4. ทัศนวิสัยและห้องขับ", "pedals_clear", "ไม่มีสิ่งของหรือขวดน้ำกีดขวางแป้นเหยียบ", inspectionMethod = "ตรวจใต้คอนโซลและพื้นห้องขับ"),

        // หมวด 5: เครื่องยนต์และของเหลว
        TemplateItemDef(5, "5. เครื่องยนต์และของเหลว", "dash_warnings", "ไฟเตือนผิดปกติหน้าปัด (เครื่องยนต์, น้ำมันเครื่อง)", inspectionMethod = "ติดเครื่องแล้วไฟเตือนต้องดับหมด"),
        TemplateItemDef(5, "5. เครื่องยนต์และของเหลว", "engine_sound", "เสียงและอาการเดินเบาเครื่องยนต์ปกติ", inspectionMethod = "ฟังเสียงเคาะหรือสั่นผิดปกติ"),
        TemplateItemDef(5, "5. เครื่องยนต์และของเหลว", "visible_leaks", "รอยรั่วหยดใต้เครื่องยนต์ที่มองเห็นได้", inspectionMethod = "ก้มดูพื้นใต้ท้องเครื่องรอบรถ"),
        TemplateItemDef(5, "5. เครื่องยนต์และของเหลว", "engine_oil_level", "ระดับน้ำมันเครื่องตามเกณฑ์ (ก้านวัด)", inspectionMethod = "ดึงก้านวัดเช็ดแล้วเสียบดูระดับ"),
        TemplateItemDef(5, "5. เครื่องยนต์และของเหลว", "coolant_level", "ระดับน้ำหล่อเย็นในหม้อพัก (ห้ามเปิดฝาหม้อน้ำร้อน!)", inspectionMethod = "ดูระดับหม้อพักน้ำใสเท่านั้น"),
        TemplateItemDef(5, "5. เครื่องยนต์และของเหลว", "other_fluids", "น้ำมันพวงมาลัยเพาเวอร์และคลัตช์", inspectionMethod = "ตรวจกระปุกน้ำมันในห้องเครื่อง"),

        // หมวด 6: ถังและอุปกรณ์ขนน้ำมัน
        TemplateItemDef(6, "6. ถังและอุปกรณ์ขนน้ำมัน", "tank_body_mounts", "สภาพตัวถังบรรจุน้ำมันและขายึดแชสซีส์", inspectionMethod = "ตรวจรอยร้าว สลัก และยางรองถัง"),
        TemplateItemDef(6, "6. ถังและอุปกรณ์ขนน้ำมัน", "valves_leaks", "ไม่มีรอยรั่วซึมที่ท่อ วาล์วถ่าย และหน้าแปลน", inspectionMethod = "ตรวจสายตาใต้ถังและตู้ควบคุมวาล์ว"),
        TemplateItemDef(6, "6. ถังและอุปกรณ์ขนน้ำมัน", "caps_covers", "ฝาปิดจุดถ่ายน้ำมันปิดล็อกแน่นหนา", inspectionMethod = "ตรวจฝาครอบและตัวล็อกวาล์ว"),
        TemplateItemDef(6, "6. ถังและอุปกรณ์ขนน้ำมัน", "hoses_couplings", "สายถ่ายน้ำมัน ข้อต่อ และซีลยางสมบูรณ์", inspectionMethod = "ตรวจข้อต่อแคมล็อกและสายยาง"),
        TemplateItemDef(6, "6. ถังและอุปกรณ์ขนน้ำมัน", "stowage_latches", "การเก็บและยึดอุปกรณ์ ปิดล็อกกล่องเครื่องมือ", inspectionMethod = "ล็อกกล่องอุปกรณ์ข้างรถให้แน่น"),
        TemplateItemDef(6, "6. ถังและอุปกรณ์ขนน้ำมัน", "discharge_pump", "สภาพภายนอกปั๊มถ่ายน้ำมัน (เฉพาะคันที่มี)", inspectionMethod = "ตรวจภายนอกปั๊มและสายพาน"),

        // หมวด 7: อุปกรณ์ประจำรถและฉุกเฉิน
        TemplateItemDef(7, "7. อุปกรณ์ประจำรถและฉุกเฉิน", "extinguisher_presence", "ถังดับเพลิงครบจำนวนและยึดแน่นในแท่น", inspectionMethod = "ตรวจตำแหน่งติดตั้งหน้ารถและข้างถัง"),
        TemplateItemDef(7, "7. อุปกรณ์ประจำรถและฉุกเฉิน", "extinguisher_condition", "เกจ์วัดถังดับเพลิงอยู่ในแถบเขียว สลักและซีลไม่ขาด", inspectionMethod = "ดูเข็มวัดแรงดันและซีลล็อก"),
        TemplateItemDef(7, "7. อุปกรณ์ประจำรถและฉุกเฉิน", "warning_triangles", "กรวยยางหรือป้ายสามเหลี่ยมสะท้อนแสงฉุกเฉิน", inspectionMethod = "ตรวจอุปกรณ์เตือนมีครบอย่างน้อย 2 ชิ้น"),
        TemplateItemDef(7, "7. อุปกรณ์ประจำรถและฉุกเฉิน", "wheel_chocks", "หมอนหนุนล้อมีพร้อมใช้งาน", inspectionMethod = "ตรวจหมอนหนุนล้ออย่างน้อย 2 อัน"),
        TemplateItemDef(7, "7. อุปกรณ์ประจำรถและฉุกเฉิน", "ppe", "อุปกรณ์คุ้มครองความปลอดภัยส่วนบุคคล (หมวก เสื้อสะท้อนแสง แว่น รองเท้าเซฟตี้)", inspectionMethod = "มีในห้องโดยสารพร้อมสวมใส่"),
        TemplateItemDef(7, "7. อุปกรณ์ประจำรถและฉุกเฉิน", "spill_kit", "อุปกรณ์ระงับเหตุหกรั่วไหล (ทราย ขี้เลื่อย หรือผ้าซับ)", inspectionMethod = "ตรวจชุดระงับเหตุฉุกเฉินประจำรถ"),

        // หมวด 8: จุดเชื่อมแม่–ลูก (เฉพาะรถเซมิเทรลเลอร์ แม่–ลูก)
        TemplateItemDef(8, "8. จุดเชื่อมแม่–ลูก", "plate_match", "ทะเบียนรถหางตรงกับคู่ที่ได้รับมอบหมายในระบบ", forVehicleType = "SEMI", inspectionMethod = "เทียบเลขทะเบียนหางกับหน้าจอ"),
        TemplateItemDef(8, "8. จุดเชื่อมแม่–ลูก", "fifth_wheel_lock", "จานลาก (Fifth Wheel) ล็อกคิงพินสนิทตามวิธีที่ช่างสอน", forVehicleType = "SEMI", inspectionMethod = "ตรวจสลักล็อกคิงพินเข้าที่สมบูรณ์"),
        TemplateItemDef(8, "8. จุดเชื่อมแม่–ลูก", "coupling_air_lines", "สายลมเชื่อมต่อแม่–ลูก (สายแดง/สายน้ำเงิน) แน่น ไม่รั่ว", forVehicleType = "SEMI", inspectionMethod = "เสียบหัวต่อลมแน่นและไม่มีเสียงลมรั่ว"),
        TemplateItemDef(8, "8. จุดเชื่อมแม่–ลูก", "coupling_electric", "สายไฟเชื่อมต่อแม่–ลูก เสียบแน่น ไฟหางติดครบ", forVehicleType = "SEMI", inspectionMethod = "ตรวจปลั๊กไฟ 7 ขั้ว"),
        TemplateItemDef(8, "8. จุดเชื่อมแม่–ลูก", "no_rubbing_lines", "สายลมและสายไฟไม่ห้อยต่ำ ไม่เสียดสีกับแชสซีส์", forVehicleType = "SEMI", inspectionMethod = "ตรวจระยะแขวนสายไฟและลม"),
        TemplateItemDef(8, "8. จุดเชื่อมแม่–ลูก", "landing_gear", "ขาค้ำยันยกสุดและล็อกคันหมุนเรียบร้อย", forVehicleType = "SEMI", inspectionMethod = "ตรวจขาค้ำยกพ้นพื้นและล็อกก้านหมุน"),

        // หมวด 9: อาการเดิมและงานค้าง
        TemplateItemDef(9, "9. อาการเดิมและงานค้าง", "prior_new_symptom", "เที่ยวก่อนหน้านี้มีอาการผิดปกติใหม่ที่ยังไม่ได้แจ้งหรือไม่", inspectionMethod = "ทบทวนการขับขี่เที่ยวล่าสุด"),
        TemplateItemDef(9, "9. อาการเดิมและงานค้าง", "prior_worsened", "อาการเดิมที่เคยแจ้งไว้ แย่ลงหรือเปลี่ยนแปลงหรือไม่", inspectionMethod = "ประเมินอาการเดิมก่อนออกรถ"),
        TemplateItemDef(9, "9. อาการเดิมและงานค้าง", "pending_acknowledged", "รับทราบรายการงานซ่อมและข้อจำกัดค้างแล้ว", inspectionMethod = "ตรวจสอบรายการแจ้งเตือนของรถ"),
        TemplateItemDef(9, "9. อาการเดิมและงานค้าง", "other_observations", "มีข้อสังเกตหรือจุดผิดปกติอื่นใดเพิ่มเติมก่อนออกรถหรือไม่", inspectionMethod = "บันทึกข้อสังเกตถ้ามี")
    )

    fun createItemsForInspection(
        inspectionId: String,
        isSemiTrailer: Boolean,
        motherVehicleId: String,
        trailerVehicleId: String?
    ): List<InspectionItemEntity> {
        val result = mutableListOf<InspectionItemEntity>()

        for (def in ALL_DEFINITIONS) {
            if (def.forVehicleType == "SEMI" && !isSemiTrailer) {
                // Skip semi coupling for 10-wheel/6-wheel
                continue
            }

            // Split lights & trailer items for semi-trailers
            if (isSemiTrailer && trailerVehicleId != null && (def.categoryId == 1 || def.categoryId == 3 || def.categoryId == 6)) {
                // Add mother item
                result.add(
                    InspectionItemEntity(
                        id = UUID.randomUUID().toString(),
                        inspectionId = inspectionId,
                        categoryId = def.categoryId,
                        categoryName = def.categoryName,
                        itemKey = "${def.itemKey}_mother",
                        itemTitle = "${def.itemTitle} (รถแม่)",
                        targetVehicleId = motherVehicleId,
                        targetLabel = "รถแม่",
                        result = null, // Mandatory: No default pass!
                        inspectionMethod = def.inspectionMethod
                    )
                )
                // Add trailer item
                result.add(
                    InspectionItemEntity(
                        id = UUID.randomUUID().toString(),
                        inspectionId = inspectionId,
                        categoryId = def.categoryId,
                        categoryName = def.categoryName,
                        itemKey = "${def.itemKey}_trailer",
                        itemTitle = "${def.itemTitle} (รถลูก)",
                        targetVehicleId = trailerVehicleId,
                        targetLabel = "รถลูก",
                        result = null, // Mandatory: No default pass!
                        inspectionMethod = def.inspectionMethod
                    )
                )
            } else if (isSemiTrailer && trailerVehicleId != null && def.categoryId == 8) {
                // Coupling item linked to trailer
                result.add(
                    InspectionItemEntity(
                        id = UUID.randomUUID().toString(),
                        inspectionId = inspectionId,
                        categoryId = def.categoryId,
                        categoryName = def.categoryName,
                        itemKey = def.itemKey,
                        itemTitle = def.itemTitle,
                        targetVehicleId = trailerVehicleId,
                        targetLabel = "จุดเชื่อมแม่–ลูก",
                        result = null,
                        inspectionMethod = def.inspectionMethod
                    )
                )
            } else {
                result.add(
                    InspectionItemEntity(
                        id = UUID.randomUUID().toString(),
                        inspectionId = inspectionId,
                        categoryId = def.categoryId,
                        categoryName = def.categoryName,
                        itemKey = def.itemKey,
                        itemTitle = def.itemTitle,
                        targetVehicleId = motherVehicleId,
                        targetLabel = if (isSemiTrailer) "รถแม่" else "ประจำคัน",
                        result = null,
                        inspectionMethod = def.inspectionMethod
                    )
                )
            }
        }

        return result
    }
}
