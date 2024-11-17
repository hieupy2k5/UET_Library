package org.example.uet_library.Controllers;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.shape.Line;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import java.time.LocalDateTime;

public class ClockController {

    @FXML
    private Line hourHand;
    @FXML
    private Line minuteHand;
    @FXML
    private Line secondHand;

    public void initialize() {
        LocalDateTime localDateTime = LocalDateTime.now();

        final double seedSecondDegrees = localDateTime.getSecond() * (360.0 / 60);
        final double seedMinuteDegrees = (localDateTime.getMinute() + seedSecondDegrees / 360.0) * (360.0 / 60);
        final double seedHourDegrees = (localDateTime.getHour() + seedMinuteDegrees / 360.0) * (360.0 / 12);

        hourHand.setStrokeWidth(5);
        minuteHand.setStrokeWidth(3);
        secondHand.setStrokeWidth(1);

        final Rotate hourRotate = new Rotate(seedHourDegrees);
        final Rotate minuteRotate = new Rotate(seedMinuteDegrees);
        final Rotate secondRotate = new Rotate(seedSecondDegrees);
        hourHand.getTransforms().add(hourRotate);
        minuteHand.getTransforms().add(minuteRotate);
        secondHand.getTransforms().add(secondRotate);

        final Timeline hourTime = new Timeline(
                new KeyFrame(
                        Duration.hours(12),
                        new KeyValue(hourRotate.angleProperty(), 360 + seedHourDegrees, Interpolator.LINEAR)
                )
        );

        final Timeline minuteTime = new Timeline(
                new KeyFrame(
                        Duration.minutes(60),
                        new KeyValue(minuteRotate.angleProperty(), 360 + seedMinuteDegrees, Interpolator.LINEAR)
                )
        );

        final Timeline secondTime = new Timeline(
                new KeyFrame(
                        Duration.seconds(60),
                        new KeyValue(secondRotate.angleProperty(), 360 + seedSecondDegrees, Interpolator.LINEAR)
                )
        );

        hourTime.setCycleCount(Animation.INDEFINITE);
        minuteTime.setCycleCount(Animation.INDEFINITE);
        secondTime.setCycleCount(Animation.INDEFINITE);

        secondTime.play();
        minuteTime.play();
        hourTime.play();
    }
}
