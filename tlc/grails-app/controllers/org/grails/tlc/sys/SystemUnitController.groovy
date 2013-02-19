package org.grails.tlc.sys

class SystemUnitController {

    // Injected services
    def utilService

    // Security settings
    def activities = [default: 'sysadmin']

    // List of actions with specific request types
    static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']

    def index() { redirect(action: 'list', params: params) }

    def list() {
        params.max = utilService.max
        params.sort = ['code', 'multiplier'].contains(params.sort) ? params.sort : 'code'
        def ddSource = utilService.source('systemMeasure.list')
        if (!ddSource) ddSource = utilService.source('systemScale.list')
        [systemUnitInstanceList: SystemUnit.selectList(), systemUnitInstanceTotal: SystemUnit.selectCount(), ddSource: ddSource]
    }

    def show() {
        def systemUnitInstance = SystemUnit.get(params.id)
        if (!systemUnitInstance) {
            flash.message = utilService.standardMessage('not.found', 'systemUnit', params.id)
            redirect(action: 'list')
        } else {
            return [systemUnitInstance: systemUnitInstance]
        }
    }

    def delete() {
        def systemUnitInstance = SystemUnit.get(params.id)
        if (systemUnitInstance) {
            try {
                utilService.deleteWithMessages(systemUnitInstance, [prefix: 'unit.name', code: systemUnitInstance.code])
                utilService.cacheService.resetThis('conversion', 0L, systemUnitInstance.code)
                flash.message = utilService.standardMessage('deleted', systemUnitInstance)
                redirect(action: 'list')
            } catch (Exception e) {
                flash.message = utilService.standardMessage('not.deleted', systemUnitInstance)
                redirect(action: 'show', id: params.id)
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'systemUnit', params.id)
            redirect(action: 'list')
        }
    }

    def edit() {
        def systemUnitInstance = SystemUnit.get(params.id)
        if (!systemUnitInstance) {
            flash.message = utilService.standardMessage('not.found', 'systemUnit', params.id)
            redirect(action: 'list')
        } else {
            return [systemUnitInstance: systemUnitInstance]
        }
    }

    def update(Long version) {
        def systemUnitInstance = SystemUnit.get(params.id)
        if (systemUnitInstance) {
            if (version != null && systemUnitInstance.version > version) {
                systemUnitInstance.errorMessage(code: 'locking.failure', domain: 'systemUnit')
                render(view: 'edit', model: [systemUnitInstance: systemUnitInstance])
                return
            }

            def oldCode = systemUnitInstance.code
            systemUnitInstance.properties['code', 'name', 'multiplier', 'measure', 'scale'] = params
            if (utilService.saveWithMessages(systemUnitInstance, [prefix: 'unit.name', code: systemUnitInstance.code, oldCode: oldCode, field: 'name'])) {
                utilService.cacheService.resetThis('conversion', 0L, systemUnitInstance.code)
                flash.message = utilService.standardMessage('updated', systemUnitInstance)
                redirect(action: 'show', id: systemUnitInstance.id)
            } else {
                render(view: 'edit', model: [systemUnitInstance: systemUnitInstance])
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'systemUnit', params.id)
            redirect(action: 'list')
        }
    }

    def create() {
        def systemUnitInstance = new SystemUnit()
        def ddSource = utilService.reSource('systemMeasure.list')
        if (ddSource) {
            systemUnitInstance.measure = ddSource
        } else {
            ddSource = utilService.reSource('systemScale.list')
            if (ddSource) systemUnitInstance.scale = ddSource
        }

        return [systemUnitInstance: systemUnitInstance]
    }

    def save() {
        def systemUnitInstance = new SystemUnit()
        systemUnitInstance.properties['code', 'name', 'multiplier', 'measure', 'scale'] = params
        if (utilService.saveWithMessages(systemUnitInstance, [prefix: 'unit.name', code: systemUnitInstance.code, field: 'name'])) {
            utilService.cacheService.resetThis('conversion', 0L, systemUnitInstance.code)
            flash.message = utilService.standardMessage('created', systemUnitInstance)
            redirect(action: 'show', id: systemUnitInstance.id)
        } else {
            render(view: 'create', model: [systemUnitInstance: systemUnitInstance])
        }
    }
}