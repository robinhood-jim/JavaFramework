#
Spring cloud webUI Demo using Spring Gateway and Oauth2.
    
    relay on microservice project : 
        spring-gateway
        spring-product-service
        ouath2
        
    spring-gateway act as url transmit and authorize,webui and gateway expose to client,oauth2 and product-service hidden in private network
        benfits:
        1.webui login to gateway logpage.gateway return random access key to webui.so client cookie only contain access key.access token and fresh_token hold
            by gateway;
        2.web client do not access oauth2 directly,so can avoid lots of network attack.
        
        wait to improvement:
        1.spring gateway has no state,and i can not found any solution to do load balance or HA.
        2.i am not use oauth2 @EnableSSO anotation,so the client and gateway expire and refresh operation need to verfiy.
        