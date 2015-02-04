
$(function () {

    var tweetsStatisticsURL = "//" + window.location.host + "/statistics"

    $.getJSON(tweetsStatisticsURL, function (data) {

        $('#container').highcharts({
            chart: {
                plotBackgroundColor: null,
                plotBorderWidth: null,
                plotShadow: false
            },
            title: {
                text: 'Tweet Sentiment Statistics'
            },
            subtitle: {
                text: 'Source: Twitter API & HP Idol Sentiment Analysis - live tweets about $HPQ.'
            },
            tooltip: {
                pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'
            },
            plotOptions: {
                pie: {
                    allowPointSelect: true,
                    cursor: 'pointer',
                    dataLabels: {
                        enabled: false
                    },
                    showInLegend: true
                }
            },
            series: [{
                type: 'pie',
                name: 'Sentiment share',
                data: data
            }]
        });
    });

});