describe('configuration page', () => {
  beforeEach(() => {
    cy.visit('/')
  })

  it('should display configuration page', () => {
    cy.get('button#config').click()

    cy.get('input[formControlName="intervals.api-key"]').should('exist')
    cy.get('input[formControlName="intervals.athlete-id"]').should('exist')
    cy.get('input[formControlName="training-peaks.auth-cookie"]').should('exist')
  })
})
