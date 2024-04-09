describe('Tests for release', {
  defaultCommandTimeout: 10000
}, () => {
  if (!Cypress.env('release')) {
    return
  }

  beforeEach(() => {
    cy.visit('/')
  })

  it('should load home page', () => {
    cy.get('button#home').click()
    cy.get('app-home').should('exist')
  })

  describe('Tests for Training Peaks', () => {
    it('should sync workout', () => {
      let mainComponent = 'tp-copy-calendar-to-calendar'

      cy.get('button#training-peaks').click()
      cy.get('app-training-peaks mat-expansion-panel:nth-child(1)').click()

      selectCalendarDate(mainComponent)

      cy.get(mainComponent).find('#btn-confirm').click()
      cy.get('mat-snack-bar-container.app-notification-success').should('exist')
    })

    it('should copy plan', () => {
      let mainComponent = 'tp-copy-library-container'
      let planName = '4DP Full Frontal Test Peak Plan by The Sufferfest.'

      cy.get('button#training-peaks').click()
      cy.get('app-training-peaks mat-expansion-panel:nth-child(2)').click()

      cy.get(mainComponent).find('mat-select[formControlName="plan"]').click()
      cy.get('mat-option').contains(`${planName} [plan, workouts: 7]`).click()
      cy.get(mainComponent).find('input[formControlName="newName"]').should('have.value', planName)
      cy.get(mainComponent).find('#btn-confirm').click()
      cy.get('mat-snack-bar-container.app-notification-success').should('exist')
    })

    it('should copy workouts from calendar', () => {
      let mainComponent = 'tp-copy-calendar-to-library'

      cy.get('button#training-peaks').click()
      cy.get('app-training-peaks mat-expansion-panel:nth-child(3)').click()

      selectCalendarDate(mainComponent)

      cy.get(mainComponent).find('#btn-confirm').click()
      cy.get('mat-snack-bar-container.app-notification-success').should('exist')
    })

  })

  describe('Tests for Trainer Road', () => {
    it('should copy workout', () => {
      let mainComponent = 'tr-copy-library-to-library'

      cy.get('button#trainer-road').click()
      cy.get('mat-expansion-panel:nth-child(1)').click()

      cy.get(mainComponent)
        .find('mat-form-field#tr-workout-name input')
        .should('be.enabled')
      cy.get(mainComponent)
        .find('mat-form-field#tr-workout-name')
        .click()
        .type('Obelisk')
      cy.get('mat-option')
        .contains(`Obelisk`)
        .click()

      cy.get(mainComponent).find('mat-select[formControlName="intervalsPlan"]').click()
      cy.get('mat-option').contains(`tp2intervals`).click()

      cy.get(mainComponent).find('#btn-confirm').click()
      cy.get('mat-snack-bar-container.app-notification-success').should('exist')
    })

    it('should copy workouts from calendar', () => {
      let mainComponent = 'tr-copy-calendar-to-library'

      cy.get('button#trainer-road').click()
      cy.get('mat-expansion-panel:nth-child(2)').click()

      selectCalendarDate(mainComponent)

      cy.get(mainComponent).find('#btn-confirm').click()
      cy.get('mat-snack-bar-container.app-notification-success').should('exist')
    })

  })

  it('should display configuration page', () => {
    cy.get('button#config').click()

    cy.get('input[formControlName="intervals.api-key"]').should('exist')
    cy.get('input[formControlName="intervals.athlete-id"]').should('exist')
    cy.get('input[formControlName="training-peaks.auth-cookie"]').should('exist')
    cy.get('input[formControlName="trainer-road.auth-cookie"]').should('exist')
  })

  function selectCalendarDate(parentComponent) {
    cy.get(parentComponent).find('mat-datepicker-toggle').click()
    cy.get('mat-datepicker-content').find('button.mat-calendar-period-button').click()
    cy.get('mat-datepicker-content').find('button.mat-calendar-body-cell').contains('2024').click()
    cy.get('mat-datepicker-content').find('button.mat-calendar-body-cell').contains('APR').click()
    cy.get('mat-datepicker-content').find('button.mat-calendar-body-cell').contains('2').click()
    cy.get('mat-datepicker-content').find('button.mat-calendar-body-cell').contains('4').click()
  }
})
