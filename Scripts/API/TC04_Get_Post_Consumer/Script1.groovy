import com.kms.katalon.core.testdata.TestDataFactory as TestDataFactory
import com.kms.katalon.core.testobject.RequestObject
import com.kms.katalon.core.testobject.ResponseObject
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import excel.ExcelHelper

/*
 * TC04 - Get Post By Id (CONSUMER)
 * Katalon mengonsumsi (consume) data dari endpoint GET /posts/{postId}
 * Data diambil dari Data Files/Request/GetPostData.csv
 */

ExcelHelper.initExcel('Report_GetPost.xlsx')

def testData = TestDataFactory.findTestData('Data Files/Request/GetPostData')

try {
    for (int i = 1; i <= testData.getRowNumbers(); i++) {
        String postId = testData.getValue('postId', i)

        RequestObject request = findTestObject('null', [
            ('postId'): postId
        ])

        ResponseObject response = WS.sendRequest(request)

        int statusCode = response.getStatusCode()
        String responseBody = response.getResponseText()
        String requestPayloadLog = "N/A (GET request, postId=${postId})"

        ExcelHelper.addRow(request.getRestUrl(), requestPayloadLog, statusCode.toString(), responseBody)

        WS.verifyResponseStatusCode(response, 200)
    }
} finally {
    ExcelHelper.saveExcel()
}
