import com.kms.katalon.core.testdata.TestDataFactory as TestDataFactory
import com.kms.katalon.core.testobject.RequestObject
import com.kms.katalon.core.testobject.ResponseObject
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import excel.ExcelHelper

/*
 * TC02 - Update Post (PRODUCER)
 * Katalon mengirim (produce) perubahan data ke endpoint PUT /posts/{postId}
 * Data diambil dari Data Files/Request/UpdatePostData.csv
 */

ExcelHelper.initExcel('Report_UpdatePost.xlsx')

def testData = TestDataFactory.findTestData('Data Files/Request/UpdatePostData')

try {
    for (int i = 1; i <= testData.getRowNumbers(); i++) {
        String postId = testData.getValue('postId', i)
        String title = testData.getValue('title', i)
        String body = testData.getValue('body', i)
        String userId = testData.getValue('userId', i)

        RequestObject request = findTestObject('null', [
            ('postId'): postId,
            ('title') : title,
            ('body')  : body,
            ('userId'): userId
        ])

        ResponseObject response = WS.sendRequest(request)

        int statusCode = response.getStatusCode()
        String responseBody = response.getResponseText()
        String requestPayloadLog = "{\"id\":${postId},\"title\":\"${title}\",\"body\":\"${body}\",\"userId\":${userId}}"

        ExcelHelper.addRow(request.getRestUrl(), requestPayloadLog, statusCode.toString(), responseBody)

        WS.verifyResponseStatusCode(response, 200)
    }
} finally {
    ExcelHelper.saveExcel()
}
