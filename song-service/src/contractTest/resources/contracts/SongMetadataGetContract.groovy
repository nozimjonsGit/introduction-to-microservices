package contracts

import org.springframework.cloud.contract.spec.Contract;

Contract.make {
    request {
        method GET()
        url "/songs/1"
    }
    response {
        status OK()
        body(
                id: "1",
                name: "Believer",
                artist: "Imagine Dragons",
                album: "Evolve",
                duration: "3:24",
                year: "2017"
        )
        headers {
            contentType('application/json')
        }
    }
}
