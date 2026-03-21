import { useDeferredValue, useState } from 'react'

function normalize(text) {
  return (text || '').trim().toLocaleLowerCase()
}

export default function SearchableSelect({
  id,
  name,
  value,
  searchValue,
  options,
  onChange,
  onSearchChange,
  placeholder,
  emptyText = 'No matches found.',
  disabled = false,
  required = false,
}) {
  const selectedOption = options.find((option) => option.value === value) ?? null
  const normalizedOptions = options.filter(
    (option) => option && option.value != null && option.label != null,
  )
  const [open, setOpen] = useState(false)
  const deferredQuery = useDeferredValue(searchValue ?? '')
  const normalizedQuery = normalize(deferredQuery)
  const filteredOptions = normalizedQuery
    ? normalizedOptions.filter((option) => normalize(option.label).includes(normalizedQuery))
    : normalizedOptions
  const inputValue = searchValue || selectedOption?.label || ''

  function handleInputChange(event) {
    const nextQuery = event.target.value
    onSearchChange?.(nextQuery)
    if (selectedOption && nextQuery !== selectedOption.label) {
      onChange('')
    }
    setOpen(true)
  }

  function handleOptionSelect(nextValue) {
    const nextOption = normalizedOptions.find((option) => option.value === nextValue) ?? null
    onSearchChange?.(nextOption?.label ?? '')
    onChange(nextValue)
    setOpen(false)
  }

  function handleBlur() {
    setTimeout(() => {
      setOpen(false)
      onSearchChange?.(selectedOption?.label ?? '')
    }, 100)
  }

  function handleFocus() {
    if (!disabled) {
      setOpen(true)
    }
  }

  function handleKeyDown(event) {
    if (event.key === 'Enter') {
      if (open && filteredOptions.length > 0) {
        event.preventDefault()
        handleOptionSelect(filteredOptions[0].value)
      }
    }

    if (event.key === 'Escape') {
      setOpen(false)
      onSearchChange?.(selectedOption?.label ?? '')
    }
  }

  return (
    <div className={`searchable-select${disabled ? ' is-disabled' : ''}`}>
      <input
        type="text"
        id={id}
        name={name}
        autoComplete="off"
        value={inputValue}
        onChange={handleInputChange}
        onFocus={handleFocus}
        onBlur={handleBlur}
        onKeyDown={handleKeyDown}
        placeholder={placeholder}
        disabled={disabled}
        required={required}
      />
      {open ? (
        <div className="searchable-select-menu">
          {filteredOptions.length > 0 ? (
            filteredOptions.slice(0, 100).map((option) => (
              <button
                key={`${option.value}-${option.label}`}
                type="button"
                className={`searchable-select-option${option.value === value ? ' is-selected' : ''}`}
                onMouseDown={(event) => {
                  event.preventDefault()
                  handleOptionSelect(option.value)
                }}
              >
                {option.label}
              </button>
            ))
          ) : (
            <div className="searchable-select-empty">{emptyText}</div>
          )}
        </div>
      ) : null}
    </div>
  )
}
