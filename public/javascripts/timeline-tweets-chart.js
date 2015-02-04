
$(function () {
    var seriesOptions = [ {
                            name: 'Negative',
                            color: 'rgba(223, 83, 83, .5)'
                            },
                            {
                                name: 'Positive',
                                color: 'rgba(60, 118, 61, .5)'
                            }];

        seriesCounter = 0,
        names = ['Negative', 'Positive'],
        createChart = function () {

            $('#container').highcharts('StockChart', {

                rangeSelector: {
                    selected: 4,
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
                    },
                    plotLines: [{
                        value: 0,
                        width: 2,
                        color: 'silver'
                    }]
                },

                plotOptions: {
                    connectNulls: true,
                    stacking: 'normal'
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

            seriesOptions[i] = {
                name: name,
                data: data
            };

            seriesCounter += 1;

            if (seriesCounter === names.length) {
                createChart();
            }
        });
    });
});