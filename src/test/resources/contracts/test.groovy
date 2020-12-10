package contracts;

import org.springframework.cloud.contract.spec.Contract

Contract.make {
	description("""
		Represents an example scenario of some contract
		
		given:
			/url given endpoint
		when:
			Make GET request
		then:
			Successful response
		
		""")
	request {
		method('GET')
		url( "/url" ){
			queryParameters {
					def INPUT_CODE = '[0-9]{3}'
					parameter 'firstParameter': value(consumer(matching(INPUT_CODE)), producer('111'))
					parameter 'secondParameter': value(consumer(matching(INPUT_CODE)), producer('222'))
				}
		}
		headers {
			contentType(applicationJson())
		}
	}
	response {
		status 200
		headers {
			contentType(applicationJson())
		}
		body([
			value      : 333
		])
		bodyMatchers {
			jsonPath('value', byRegex(aDouble()).asDouble())
		}
	}
}