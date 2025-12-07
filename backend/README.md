## End-Points

POST   /auth/account/register                    Register user (CLIENT or COACH)
GET    /auth/account/resend?email=               Resend activation email
GET    /auth/account/activate?token=             Activate account
GET    /auth/invitation/accept?token=            Accept invitation
GET    /auth/password/forgot?email=              Send mail for forgot password
POST   /auth/password/reset                      Reset password

GET    /user                                     Get user data
GET    /user/profile                             Get user profile
PUT    /user/profile                             Sets or updates user profile
POST   /user/password                            Change password

GET    /coach                                    Get coach
PATCH  /coach                                    Update coach
DELETE /coach                                    Remove coach
GET    /coach/clients                            Get clients from a coach
GET    /coach/clients/{clientId}                 Get a client from a coach
POST   /coach/clients/invite?email=              Send invitation
GET    /coach/clients/{clientId}/nutrition-plan  Get clients nutrition plan
POST   /coach/clients/{clientId}/nutrition-plan  Add nutrition plan for client
GET    /coach/clients/{clientId}/nutrition-plans Get clients nutrition plans

GET    /client                                   Get client
PATCH  /client                                   Update client
GET    /client/nutrition-plan                    Get clients nutrition plan
GET    /client/nutrition-plans                   Get clients nutrition plans
