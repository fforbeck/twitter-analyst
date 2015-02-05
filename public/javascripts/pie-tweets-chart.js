
$(function () {

    var tweetsStatisticsURL = "//" + window.location.host + "/statistics"

    $.getJSON(tweetsStatisticsURL, function (data) {

        $('#container').highcharts({
            chart: {
                plotBackgroundColor: null,
                plotBorderWidth: null,
                plotShadow: false
            },
            colors: ['rgba(60, 118, 61, .7)','rgba(223, 83, 83, .7)','rgba(214, 209, 209, .9)'],
            title: {
                text: 'Tweet Sentiment Statistics'
            },
            subtitle: {
                text: 'Source: Twitter API & HP Idol Sentiment Analysis - live tweets about $HPQ.'
            },
            tooltip: {
                pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b><br/>{point.count} tweets'
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