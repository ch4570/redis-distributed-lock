package com.redis.example.common

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringTestExtension
import io.kotest.extensions.spring.SpringTestLifecycleMode
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.transaction.annotation.Transactional

@Transactional
@SpringBootTest
@Import(TransactionSupport::class)
abstract class AbstractServiceTest : BehaviorSpec({
    extensions(SpringTestExtension(SpringTestLifecycleMode.Root))
    isolationMode = IsolationMode.InstancePerLeaf
})