import com.kms.katalon.core.testdata.TestDataFactory as TestDataFactory
import com.kms.katalon.core.testobject.RequestObject
import com.kms.katalon.core.testobject.ResponseObject
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import excel.ExcelHelper

/*
 * TC03 - Delete Post (PRODUCER)
 * Katalon mengirim (produce) perintah hapus ke endpoint DELETE /posts/{postId}
 * Data diambil dari Data Files/Request/DeletePostData.csv
 */

ExcelHelper.initExcel('Report_DeletePost.xlsx')

def testData = TestDataFactory.findTestData('Data Files/Request/DeletePostData')

try {
    for (int i = 1; i <= testData.getRowNumbers(); i++) {
        String postId = testData.getValue('postId', i)

        RequestObject request = findTestObject('null', [
            ('postId'): postId
        ])

        ResponseObject response = WS.sendRequest(request)

        int statusCode = response.getStatusCode()
        String responseBody = response.getResponseText()
        String requestPayloadLog = "N/A (DELETE tidak memiliki body, postId=${postId})"

        ExcelHelper.addRow(request.getRestUrl(), requestPayloadLog, statusCode.toString(), responseBody)

        WS.verifyResponseStatusCode(response, 200)
    }
} finally {
    ExcelHelper.saveExcel()
}
