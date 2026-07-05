package excel

import com.kms.katalon.core.annotation.Keyword
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.Font
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook

import java.text.SimpleDateFormat

/**
 * Custom Keyword untuk mencatat hasil pengujian API / Kafka ke dalam file Excel.
 *
 * Format kolom: No | Endpoint | Request Payload | Status Code | Response Body | Timestamp
 *
 * Cara pakai di Test Case:
 *   CustomKeywords.'excel.ExcelHelper.initExcel'('Report_API_Test.xlsx')
 *   CustomKeywords.'excel.ExcelHelper.addRow'(endpoint, requestPayload, statusCode, responseBody)
 *   CustomKeywords.'excel.ExcelHelper.saveExcel'()
 */
class ExcelHelper {

    private static Workbook workbook
    private static Sheet sheet
    private static String filePath
    private static int rowCounter = 1 // baris 0 = header

    private static final List<String> HEADERS = ['No', 'Endpoint', 'Request Payload', 'Status Code', 'Response Body', 'Timestamp']

    /**
     * Inisialisasi workbook Excel baru. Jika file sudah ada pada folder yang sama
     * di run sebelumnya, file lama akan ditimpa (overwrite) supaya laporan selalu fresh.
     * Hasil disimpan di folder: <Project>/Reports/Excel/<fileName>
     */
    @Keyword
    static void initExcel(String fileName) {
        workbook = new XSSFWorkbook()
        sheet = workbook.createSheet('Test Results')
        rowCounter = 1

        String projectDir = com.kms.katalon.core.configuration.RunConfiguration.getProjectDir()
        File outputDir = new File(projectDir, 'Reports/Excel')
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }
        filePath = new File(outputDir, fileName).getAbsolutePath()

        // Buat header row dengan style bold
        Row headerRow = sheet.createRow(0)
        Font headerFont = workbook.createFont()
        headerFont.setBold(true)
        CellStyle headerStyle = workbook.createCellStyle()
        headerStyle.setFont(headerFont)

        HEADERS.eachWithIndex { String headerText, int idx ->
            Cell cell = headerRow.createCell(idx)
            cell.setCellValue(headerText)
            cell.setCellStyle(headerStyle)
        }
    }

    /**
     * Menambahkan satu baris data hasil pengujian ke sheet Excel.
     *
     * @param endpoint          URL endpoint / topic Kafka yang diuji
     * @param requestPayload    Isi request yang dikirim (body/params), boleh kosong untuk GET/DELETE/Kafka consumer
     * @param statusCode        HTTP status code, atau status custom (misal 'CONSUMED') untuk Kafka
     * @param responseBody      Isi response / pesan yang diterima
     */
    @Keyword
    static void addRow(String endpoint, String requestPayload, String statusCode, String responseBody) {
        if (workbook == null || sheet == null) {
            throw new IllegalStateException('ExcelHelper belum diinisialisasi. Panggil initExcel() terlebih dahulu.')
        }

        Row row = sheet.createRow(rowCounter)
        row.createCell(0).setCellValue(rowCounter) // No
        row.createCell(1).setCellValue(endpoint ?: '')
        row.createCell(2).setCellValue(requestPayload ?: '')
        row.createCell(3).setCellValue(statusCode ?: '')
        row.createCell(4).setCellValue(responseBody ?: '')

        SimpleDateFormat sdf = new SimpleDateFormat('yyyy-MM-dd HH:mm:ss')
        row.createCell(5).setCellValue(sdf.format(new Date()))

        rowCounter++
    }

    /**
     * Menyimpan (flush) workbook ke file .xlsx dan menutup resource.
     * Wajib dipanggil di akhir Test Case (idealnya di block finally / Listener AfterTestCase).
     */
    @Keyword
    static void saveExcel() {
        if (workbook == null || filePath == null) {
            return
        }
        // Auto-size kolom supaya rapi
        (0..<HEADERS.size()).each { int col ->
            sheet.autoSizeColumn(col)
        }

        FileOutputStream fos = new FileOutputStream(filePath)
        try {
            workbook.write(fos)
        } finally {
            fos.close()
        }
        workbook.close()

        println("Excel report berhasil disimpan di: ${filePath}")

        workbook = null
        sheet = null
        rowCounter = 1
    }
}
