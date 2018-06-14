package fi.vm.yti.taxgen.yclsourceparser.sourcebundle.ycl

import fi.vm.yti.taxgen.yclsourceparser.ext.jackson.nonBlankTextOrNullAt
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.helpers.HttpOps
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.helpers.JacksonObjectMapper

class YclPagedResourceRetriever(
    url: String
) : AbstractIterator<String>() {

    private var nextPageUrl: String? = composeInitialPagedUrl(url)

    override fun computeNext() {
        val url = nextPageUrl

        if (url != null) {
            val resource = HttpOps.getJsonData(url)
            resolveNextPageUrl(resource)
            setNext(resource)
        } else {
            done()
        }
    }

    private fun composeInitialPagedUrl(url: String): String {
        return "$url?pageSize=1000"
    }

    private fun resolveNextPageUrl(response: String) {
        val responseJson =
            JacksonObjectMapper.lenientObjectMapper().readTree(response) ?: throw YclCodeList.InitFailException()

        val nextPageUrl = responseJson.nonBlankTextOrNullAt("/meta/nextPage")
        check(this.nextPageUrl != nextPageUrl) { "Service returned identical next page url" }
        this.nextPageUrl = nextPageUrl
    }
}
