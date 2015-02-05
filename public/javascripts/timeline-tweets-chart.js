
$(function () {
    var seriesOptions = [];

        seriesCounter = 0,
        names = ['Positive', 'Negative'],
        createChart = function () {

            $('#container').highcharts('StockChart', {
                rangeSelector: {
                    selected: 2
                },
                title: {
                    text: 'Sentiment Score VS Date Time '
                },
                subtitle: {
                    text: 'Source: Twitter API & HP Idol Sentiment Analysis - Tweets timeline about $HPQ.'
                },
                yAxis: {
                    labels: {
                        formatter: function () {
                            return (this.value > 0 ? ' + ' : '') + this.value + '%';
                        }
                    }
                },
                tooltip: {
                    pointFormat: '<span style="color:{series.color}">{series.name}</span>: <b>{point.y}</b> ' +
                    ' <br /> {point.user} <br/> {point.tweet}<br/>',
                    valueDecimals: 2
                },
                series: seriesOptions
            });
        };

    $.each(names, function (i, name) {

        var tweetsBySentimentURL = "//" + window.location.host + "/tweets/" + name.toLowerCase();

        $.getJSON(tweetsBySentimentURL, function (data) {
            var color = 'rgba(60, 118, 61, .5)';
            if (name === 'Negative') {
                color = 'rgba(223, 83, 83, .5)';
            }
            seriesOptions[i] = {
                name: name,
                data: data,
                color: color,
                lineWidth : 0,
                marker : {
                    enabled : true,
                    radius : 2
                },
                tooltip: {
                    valueDecimals: 2
                }
            };
            seriesCounter += 1;

            if (seriesCounter === names.length) {
                createChart();
            }
        });
    });
});