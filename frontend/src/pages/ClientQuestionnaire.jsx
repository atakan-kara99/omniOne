import { useState } from 'react'
import {
  getClientAnswers,
  getClientQuestionnaire,
  updateClientAnswers,
} from '../api.js'
import { useLoadData } from '../hooks/useLoadData.js'
import { useFormState } from '../hooks/useFormState.js'
import PagePanel from '../components/PagePanel.jsx'
import FormField from '../components/FormField.jsx'
import StatusMessage from '../components/StatusMessage.jsx'
import Button from '../components/Button.jsx'

function ClientQuestionnaire() {
  const [questions, setQuestions] = useState([])
  const [answers, setAnswers] = useState({})
  const form = useFormState()

  const { loading, error: loadError } = useLoadData(async () => {
    const [questionList, answerList] = await Promise.all([
      getClientQuestionnaire(),
      getClientAnswers().catch((err) => (err.status === 404 ? [] : Promise.reject(err))),
    ])
    setQuestions(questionList || [])
    const answerMap = {}
    ;(answerList || []).forEach((item) => {
      answerMap[item.questionId] = item.answerText || ''
    })
    setAnswers(answerMap)
  }, [])

  async function handleSubmit(event) {
    event.preventDefault()
    form.reset()
    form.startSaving()
    try {
      const payload = questions.map((question) => ({
        questionId: question.id,
        answer: answers[question.id] || '',
      }))
      await updateClientAnswers(payload)
      form.setSuccess('Answers saved.')
    } catch (err) {
      form.setFailure(err || 'Failed to save answers.')
    }
  }

  return (
    <PagePanel
      title="Your questionnaire"
      subtitle="Update your intake answers to keep your plan precise."
      loading={loading}
      error={loadError}
    >
      {questions.length === 0 ? (
        <p className="muted">No questions available yet.</p>
      ) : (
        <form className="form" onSubmit={handleSubmit} autoComplete="off">
          {questions.map((question) => (
            <FormField
              key={question.id}
              label={question.text}
              error={form.fieldErrors?.[`question-${question.id}`]}
            >
              <input
                type="text"
                name={`question-${question.id}`}
                id={`client-question-${question.id}`}
                autoComplete="off"
                value={answers[question.id] || ''}
                onChange={(event) =>
                  setAnswers((prev) => ({
                    ...prev,
                    [question.id]: event.target.value,
                  }))
                }
              />
            </FormField>
          ))}
          <StatusMessage status={form.status} error={form.error} />
          <Button type="submit" loading={form.saving} loadingText="Saving...">
            Save answers
          </Button>
        </form>
      )}
    </PagePanel>
  )
}

export default ClientQuestionnaire
