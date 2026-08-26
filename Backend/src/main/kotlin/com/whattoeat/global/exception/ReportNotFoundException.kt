package com.whattoeat.global.exception

class ReportNotFoundException(id: Long) :
    RuntimeException("신고를 찾을 수 없습니다: $id")
