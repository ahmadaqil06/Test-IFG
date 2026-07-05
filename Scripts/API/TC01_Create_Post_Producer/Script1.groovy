import com.kms.katalon.core.testdata.TestDataFactory as TestDataFactory
import com.kms.katalon.core.testobject.RequestObject
import com.kms.katalon.core.testobject.ResponseObject
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import excel.ExcelHelper

/*
 * TC01 - Create Post (PRODUCER)
 * Katalon mengirim (produce) data baru ke endpoint POST /posts.
 * Request diambil dari Object Repository yang sudah di-parameterize
 * menggunakan ${title}, ${body}, ${userId}.
 * Data diambil dari Data Files/Request/CreatePostData.csv
 * Setiap response dicatat ke Excel: Reports/Excel/Report_CreatePost.xlsx
 */

ExcelHelper.initExcel('Report_CreatePost.xlsx')

def testData = TestDataFactory.findTestData('Data Files/Request/CreatePostData')

try {
    for (int i = 1; i <= testData.getRowNumbers(); i++) {
        String title = testData.getValue('title', i)
        String body = testData.getValue('body', i)
        String userId = testData.getValue('userId', i)

        // Binding value ke variable ${title}, ${body}, ${userId} yang ada di Object Repository
        RequestObject request = findTestObject('null', [
            ('title') : title,
            ('body')  : body,
            ('userId'): userId
        ])

        ResponseObject response = WS.sendRequest(request)

        int statusCode = response.getStatusCode()
        String responseBody = response.getResponseText()
        String requestPayloadLog = "{\"title\":\"${title}\",\"body\":\"${body}\",\"userId\":${userId}}"

        ExcelHelper.addRow(request.getRestUrl(), requestPayloadLog, statusCode.toString(), responseBody)

        WS.verifyResponseStatusCode(response, 201)
    }
} finally {
    ExcelHelper.saveExcel()
}
