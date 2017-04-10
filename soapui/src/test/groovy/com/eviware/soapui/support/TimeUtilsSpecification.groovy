package com.eviware.soapui.support

import spock.lang.Specification

class TimeUtilsSpecification extends Specification {

    static final second = 1000L
    static final minute = 60L * second
    static final hour = 60L * minute
    static final day = 24L * hour

    def "TimeUtils formats time periods as expected by the business"() {
        when:
        def result = TimeUtils.formatTimeDuration(start, end)

        then:
        result == expectedValue

        where:
        start   | end                                                        | expectedValue
        0L      | 0L                                                         | '00:00:00'
        0L      | 1L                                                         | '00:00:00'
        1000L   | 1000L                                                      | '00:00:00'
        0L      | 8L * second                                                | '00:00:08'
        0L      | 31L * minute                                               | '00:31:00'
        0L      | 18L * hour                                                 | '18:00:00'
        0L      | (4L * hour) + (3L * minute) + (2L * second)                | '04:03:02'
        0L      | (4L * hour) + (3L * minute) + (2L * second) + 586L         | '04:03:02'
        0L      | day + (12L * hour)                                         | '1 day, 12:00:00'
        0L      | (2 * day) + (5L * hour) + (25L * minute) + (14L * second)  | '2 days, 05:25:14'
        0L      | (28 * day) + (5L * hour) + (25L * minute) + (14L * second) | '4 weeks, 05:25:14'
        0L      | (29 * day) + (5L * hour) + (25L * minute) + (14L * second) | '4 weeks, 1 day, 05:25:14'
        0L      | (37 * day) + (5L * hour) + (25L * minute) + (14L * second) | '1 month, 6 days, 05:25:14'
        0L      | (68 * day) + (5L * hour) + (25L * minute) + (14L * second) | '2 months, 1 week, 2 days, 05:25:14'
        5000L   | 5000L + (5L * second)                                      | '00:00:05'
        3 * day | (3 * day) + (4L * hour) + (3L * minute) + (2L * second)    | '04:03:02'

    }

}
