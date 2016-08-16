package hello;

import hello.wsdl.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.client.core.SoapActionCallback;

import java.text.SimpleDateFormat;

/**
 * @author Khaled Ayoubi
 */
public class WeatherClient extends WebServiceGatewaySupport {

    private static final Logger log = LoggerFactory.getLogger(WeatherClient.class);

    public GetCityForecastByZIPResponse getCityForecastByZip(String zipCode) {

        GetCityForecastByZIP request = new GetCityForecastByZIP();
        request.setZIP(zipCode);

        log.info("Requesting forecast for " + zipCode);

        GetCityForecastByZIPResponse response = (GetCityForecastByZIPResponse) getWebServiceTemplate()
            .marshalSendAndReceive(
                "http://wsf.cdyne.com/WeatherWS/Weather.asmx",
                request,
                new SoapActionCallback("http://ws.cdyne.com/WeatherWS/GetCityForecastByZIP"));

        printResponse(response);

        return response;
    }

    private void printResponse(GetCityForecastByZIPResponse response) {

        ForecastReturn forecastReturn = response.getGetCityForecastByZIPResult();

        if (forecastReturn.isSuccess()) {
            log.info("Forecast for " + forecastReturn.getCity() + ", " + forecastReturn.getState());

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            for (Forecast forecast : forecastReturn.getForecastResult().getForecast()) {

                Temp temperature = forecast.getTemperatures();

                log.info(String.format("%s %s %s°-%s°", format.format(forecast.getDate().toGregorianCalendar().getTime()),
                    forecast.getDesciption(), temperature.getMorningLow(), temperature.getDaytimeHigh()));
                log.info("");
            }
        } else {
            log.info("No forecast received");
        }
    }

    public static void main(String[] args) {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath("hello.wsdl");

        WeatherClient client = new WeatherClient();
        client.setDefaultUri("http://ws.cdyne.com/WeatherWS");
        client.setMarshaller(marshaller);
        client.setUnmarshaller(marshaller);

        client.getCityForecastByZip("94304");
    }
}
